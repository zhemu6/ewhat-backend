package com.lushihao.ewhatbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.CanteenDTO;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.service.CanteenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-食堂管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   16:45
 */
@Slf4j
@RestController("adminCanteenController")
@RequestMapping("/admin/canteen")
public class CanteenController {

    @Resource
    private CanteenService canteenService;

    /**
     * 根据id查询食堂
     * @param id 食堂id
     * @return 食堂
     */
    @GetMapping("/{id}")
    public BaseResponse<Canteen> queryCanteenById(@PathVariable("id") Long id){
        Canteen canteen =  canteenService.queryCanteenById(id);
        return ResultUtils.success(canteen);
    }
    /**
     * 查询一个学校下对应的所有食堂
     * @param schoolId 学校id
     * @return list食堂列表
     */
    @GetMapping("/listBySchool/{id}")
    public BaseResponse<List<Canteen>> queryCanteenBySchool(@PathVariable("id") Long schoolId){
        List<Canteen> canteens =  canteenService.queryCanteenBySchoolId(schoolId);
        return ResultUtils.success(canteens);
    }


    /**
     * 添加食堂
     * @param canteenDTO 前端传入食堂对象
     * @return 新增后食堂id
     */
    @PostMapping
    public BaseResponse<Long> save(@RequestBody CanteenDTO canteenDTO){
        Long id = canteenService.save(canteenDTO);
        // 返回食堂id
        return ResultUtils.success(id);
    }

    /**
     * 更新食堂信息
     * @param canteen 前端传入食堂信息
     * @return 是否更新成功
     */
    @PutMapping
    public BaseResponse<Boolean> updateCanteen(@RequestBody Canteen canteen){
        Boolean success = canteenService.updateCanteen(canteen);
        return  ResultUtils.success(success);
    }

    /**
     * 分页查询食堂
     * @param pageRequest 分页查询请求
     * @return 分页查询结果
     */
    @GetMapping("/page")
    public BaseResponse<Page<Canteen>> pageCanteens(PageRequest pageRequest){
        Page<Canteen> page = canteenService.listCanteensByPage(pageRequest);
        return ResultUtils.success(page);
    }


}
