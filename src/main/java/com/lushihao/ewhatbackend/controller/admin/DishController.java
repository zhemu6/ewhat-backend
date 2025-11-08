package com.lushihao.ewhatbackend.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.DishDTO;
import com.lushihao.ewhatbackend.model.entity.Dish;
import com.lushihao.ewhatbackend.service.DishService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-菜品餐品管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-05   16:39
 */
@Slf4j
@RestController("adminDishController")
@RequestMapping("/admin/dish")
public class DishController {
    @Resource
    private DishService dishService;

    /**
     * 根据id查询菜品
     * @param id 菜品id
     * @return 菜品
     */
    @GetMapping("/{id}")
    public BaseResponse<Dish> queryDishById(@PathVariable("id") Long id){
        Dish dish =  dishService.queryDishById(id);
        return ResultUtils.success(dish);
    }
    /**
     * 查询一个食堂下对应的所有菜品
     * @param canteenId 食堂id
     * @return list菜品列表
     */
    @GetMapping("/listByCanteen/{id}")
    public BaseResponse<List<Dish>> queryDishByCanteen(@PathVariable("id") Long canteenId){
        List<Dish> dishs =  dishService.queryDishByCanteenId(canteenId);
        return ResultUtils.success(dishs);
    }

    /**
     * 添加菜品
     * @param dishDTO 前端传入菜品对象
     * @return 新增后菜品id
     */
    @PostMapping
    public BaseResponse<Long> save(@RequestBody DishDTO dishDTO){
        Long id = dishService.save(dishDTO);
        // 返回菜品id
        return ResultUtils.success(id);
    }

    /**
     * 更新菜品信息
     * @param dish 前端传入菜品信息
     * @return 是否更新成功
     */
    @PutMapping
    public BaseResponse<Boolean> updateDish(@RequestBody Dish dish){
        Boolean success = dishService.updateDish(dish);
        return  ResultUtils.success(success);
    }

    /**
     * 分页查询菜品
     * @param pageRequest 分页查询请求
     * @return 分页查询结果
     */
    @GetMapping("/page")
    public BaseResponse<Page<Dish>> pageDishs(PageRequest pageRequest){
        Page<Dish> page = dishService.listDishsByPage(pageRequest);
        return ResultUtils.success(page);
    }

}
