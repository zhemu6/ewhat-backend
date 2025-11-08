package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.constant.StatusConstant;
import com.lushihao.ewhatbackend.constant.SystemConstants;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.CanteenDTO;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.entity.School;
import com.lushihao.ewhatbackend.model.vo.CanteenVO;
import com.lushihao.ewhatbackend.service.CanteenService;
import com.lushihao.ewhatbackend.mapper.CanteenMapper;
import com.lushihao.ewhatbackend.service.SchoolService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

import static com.lushihao.ewhatbackend.constant.RedisConstants.SCHOOL_GEO_KEY;

/**
* @author lushihao
* @description 针对表【tb_canteen(学校信息)】的数据库操作Service实现
* @createDate 2025-10-22 17:56:01
*/
@Service
public class CanteenServiceImpl extends ServiceImpl<CanteenMapper, Canteen>
    implements CanteenService{
    @Resource
    private SchoolService schoolService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Canteen queryCanteenById(Long id) {
        Canteen canteen = this.getById(id);
        ThrowUtils.throwIf(canteen == null, ErrorCode.NOT_FOUND_ERROR, "请求食堂不存在！");
        return canteen;
    }

    @Override
    public Long save(CanteenDTO canteenDTO) {
        Canteen canteen = BeanUtil.copyProperties(canteenDTO, Canteen.class);
        // 1. 添加食堂之前，判断其绑定的学校是否存在
        Long schoolId = canteen.getSchoolId();
        School school = schoolService.getById(schoolId);
        ThrowUtils.throwIf(school==null,ErrorCode.NOT_FOUND_ERROR,"添加食堂失败，选择的学校不存在");
        // 2. 添加食堂
        try {
            boolean success = this.save(canteen);
            ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "添加食堂失败，请稍后重试！");
        } catch (DuplicateKeyException e) {
            ThrowUtils.throwIf(true, ErrorCode.DATA_EXIST_ERROR, "添加食堂失败，数据已存在！");
        }
        return canteen.getId();
    }

    @Override
    public Boolean updateCanteen(Canteen canteen) {
        // 1. 判断是否存在
        boolean success = this.updateById(canteen);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "更新食堂失败，请稍后重试！");
        return true;
    }

    @Override
    public Page<Canteen> listCanteensByPage(PageRequest pageRequest) {
        // 构建分页对象
        Page<Canteen> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        // 构建查询条件
        QueryWrapper<Canteen> queryWrapper = new QueryWrapper<>();
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        }
        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Canteen> queryCanteenBySchoolId(Long schoolId) {
        School school = schoolService.getById(schoolId);
        ThrowUtils.throwIf(school==null,ErrorCode.NOT_FOUND_ERROR,"该学校不存在");
        QueryWrapper<Canteen> wrapper = new QueryWrapper<Canteen>().eq("school_id", schoolId);
        return this.list(wrapper);
    }

    @Override
    public List<CanteenVO> listCanteenVOsBySchoolByList(Long schoolId) {
        return this.queryCanteenBySchoolId(schoolId)
                .stream()
                .filter(canteen -> StatusConstant.ENABLE.equals(canteen.getStatus()))
                .map(canteen -> BeanUtil.copyProperties(canteen, CanteenVO.class))
                .toList();
    }

    @Override
    public List<CanteenVO> queryCanteenVOsBySchoolId(Long schoolId, Integer current, Double x, Double y,Integer distanceValue) {
        // 1.如果xy为空 代表不需要查询坐标 直接从数据库中查询即可
        if (x == null || y == null) {
            Page<Canteen> page = query()
                    .eq("school_id", schoolId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return page.getRecords().stream()
                    .map(canteen -> BeanUtil.copyProperties(canteen, CanteenVO.class)).toList();
        }
        // 2. 计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = SCHOOL_GEO_KEY + schoolId;
        // 3.从redis中查询，按照距离排序 分页获得canteenId 和 距离
        GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = stringRedisTemplate.opsForGeo()
                .search(key,
                        GeoReference.fromCoordinate(x, y),// 查询参考点，用给定的经纬度作为中心点
                        new Distance(distanceValue), // 搜索半径范围（5000米 = 5公里）
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                                .includeDistance() // 在结果中包含距离
                                .limit(end)// 限制返回数量，比如前 N 条
                );
        if(geoResults==null){
            return Collections.emptyList();
        }
        // 4.解析出来id
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = geoResults.getContent();
        if(content.size()<=from){
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>(content.size());
        // distanceMap中 key是canteenId Value是Distance
        Map<String, Distance> distanceMap = new HashMap<>(content.size());
        content.stream().skip(from).forEach(result->{
            // 4.2获取店铺id
            String canteenIdStr = result.getContent().getName();
            // 获取distance
            Distance distance = result.getDistance();
            ids.add(Long.valueOf(canteenIdStr));
            distanceMap.put(canteenIdStr,distance);
        });
        // 5.根据id查询食堂
        String idStr = StrUtil.join(",", ids);
        List<CanteenVO> canteenVOS = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list().stream().map(canteen -> BeanUtil.copyProperties(canteen, CanteenVO.class)).toList();
        for (CanteenVO canteenVO:canteenVOS){
            canteenVO.setDistance(distanceMap.get(canteenVO.getId().toString()).getValue());
        }
        return canteenVOS;
    }
}




