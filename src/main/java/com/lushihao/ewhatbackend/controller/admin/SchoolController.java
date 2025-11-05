package com.lushihao.ewhatbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.SchoolDTO;
import com.lushihao.ewhatbackend.model.entity.School;
import com.lushihao.ewhatbackend.service.SchoolService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学校管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   16:45
 */
@Slf4j
@RestController("adminSchoolController")
@RequestMapping("/admin/school")
public class SchoolController {
    @Resource
    private SchoolService schoolService;

    /**
     * 根据id查询学校
     * @param id 学校id
     * @return 学校视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<School> querySchoolById(@PathVariable("id") Long id){
        School school =  schoolService.querySchoolById(id);
        return ResultUtils.success(school);
    }

    /**
     * 添加学校
     * @param schoolDTO 前端传入学校对象
     * @return 新增后学校id
     */
    @PostMapping
    public BaseResponse<Long> save(@RequestBody SchoolDTO schoolDTO){
        Long id = schoolService.save(schoolDTO);
        // 返回学校id
        return ResultUtils.success(id);
    }

    /**
     * 更新学校信息
     * @param school 前端传入学校信息
     * @return 是否更新成功
     */
    @PutMapping
    public BaseResponse<Boolean> updateSchool(@RequestBody School school){
        Boolean success = schoolService.updateSchool(school);
        return  ResultUtils.success(success);
    }

    /**
     * 分页查询学校
     * @param pageRequest 分页查询请求
     * @return 分页查询结果
     */
    @GetMapping("/page")
    public BaseResponse<Page<School>> pageSchools(PageRequest pageRequest){
        Page<School> page = schoolService.listSchoolsByPage(pageRequest);
        return ResultUtils.success(page);
    }

}
