package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.constant.StatusConstant;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.SchoolDTO;
import com.lushihao.ewhatbackend.model.entity.School;
import com.lushihao.ewhatbackend.model.vo.SchoolVO;
import com.lushihao.ewhatbackend.service.SchoolService;
import com.lushihao.ewhatbackend.mapper.SchoolMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lushihao
 * @description 针对表【tb_school(学校信息)】的数据库操作Service实现
 * @createDate 2025-10-22 16:42:18
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School>
        implements SchoolService {
    /**
     * 根据id查询学校
     *
     * @param id 学校id
     * @return 学校视图对象
     */
    @Override
    public School querySchoolById(Long id) {
        School school = this.getById(id);
        ThrowUtils.throwIf(school == null, ErrorCode.NOT_FOUND_ERROR, "请求学校不存在！");
        return school;
    }

    /**
     * 添加学校
     *
     * @param schoolDTO 前端传入学校对象
     * @return 新增后学校id
     */
    @Override
    public Long save(SchoolDTO schoolDTO) {
        School school = BeanUtil.copyProperties(schoolDTO, School.class);
        try {
            boolean success = this.save(school);
            ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "添加学校失败，请稍后重试！");
        } catch (DuplicateKeyException e) {
            ThrowUtils.throwIf(true, ErrorCode.DATA_EXIST_ERROR, "添加学校失败，数据已存在！");
        }
        return school.getId();
    }
    /**
     * 更新学校信息
     * @param school 前端传入学校信息
     * @return 是否更新成功
     */
    @Override
    public Boolean updateSchool(School school) {
        // 1. 判断是否存在
        boolean success = this.updateById(school);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "更新学校失败，请稍后重试！");
        return true;
    }
    /**
     * 分页查询学校
     * @param pageRequest 分页查询请求
     * @return 分页查询结果
     */
    @Override
    public Page<School> listSchoolsByPage(PageRequest pageRequest) {
        // 构建分页对象
        Page<School> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());

        // 构建查询条件
        QueryWrapper<School> queryWrapper = new QueryWrapper<>();
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        }

        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public List<SchoolVO> listSchoolVOsByList() {
        return this.list()
                .stream()
                .filter(school -> StatusConstant.ENABLE.equals(school.getStatus()))
                .map(school -> BeanUtil.copyProperties(school, SchoolVO.class))
                .toList();
    }
}




