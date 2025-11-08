package com.lushihao.ewhatbackend.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.DishDTO;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.entity.Dish;
import com.lushihao.ewhatbackend.model.vo.CanteenVO;
import com.lushihao.ewhatbackend.model.vo.DishVO;
import com.lushihao.ewhatbackend.service.DishService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端-菜品餐品管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-05   16:39
 */
@Slf4j
@RestController("userDishController")
@RequestMapping("/user/dish")
public class DishController {

    @Resource
    private DishService dishService;

    /**
     * 根据id查询菜品
     * @param id 菜品id
     * @return 菜品视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<DishVO> queryDishById(@PathVariable("id") Long id){
        Dish dish =  dishService.queryDishById(id);
        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
        return ResultUtils.success(dishVO);
    }

    /**
     * 查询食堂的菜品列表
     * @return 查询结果 list集合
     */
    @GetMapping("/listByCanteen/{id}")
    public BaseResponse<List<DishVO>> listDishVOsByCanteen(@PathVariable("id") Long canteenId){
        List<DishVO> dishs  = dishService.listDishVOsByCanteenByList(canteenId);
        return ResultUtils.success(dishs);
    }

}
