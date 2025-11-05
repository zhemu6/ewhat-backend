package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.constant.StatusConstant;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

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
}




