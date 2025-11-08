package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.constant.StatusConstant;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.DishDTO;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.entity.Dish;
import com.lushihao.ewhatbackend.model.entity.School;
import com.lushihao.ewhatbackend.model.vo.DishVO;
import com.lushihao.ewhatbackend.service.CanteenService;
import com.lushihao.ewhatbackend.service.DishService;
import com.lushihao.ewhatbackend.mapper.DishMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_dish(餐品信息)】的数据库操作Service实现
* @createDate 2025-11-05 16:35:31
*/
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
    implements DishService {
    @Resource
    private CanteenService canteenService;


    @Override
    public Dish queryDishById(Long id) {
        Dish dish = this.getById(id);
        ThrowUtils.throwIf(dish == null, ErrorCode.NOT_FOUND_ERROR, "请求菜品不存在！");
        return dish;
    }

    @Override
    public List<Dish> queryDishByCanteenId(Long canteenId) {
        // 1.获取食堂信息
        Canteen canteen = canteenService.getById(canteenId);
        ThrowUtils.throwIf(canteen==null,ErrorCode.NOT_FOUND_ERROR,"该食堂不存在");
        QueryWrapper<Dish> wrapper = new QueryWrapper<Dish>().eq("canteen_id", canteenId);
        return this.list(wrapper);
    }

    @Override
    public Long save(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        // 1.添加菜品之前，判断其绑定的餐厅是否存在
        Long canteenId = dish.getCanteenId();
        Canteen canteen = canteenService.getById(canteenId);
        ThrowUtils.throwIf(canteen==null,ErrorCode.NOT_FOUND_ERROR,"添加菜品失败，选择的食堂不存在");
        // 2.添加菜品
        try {
            boolean success = this.save(dish);
            ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "添加菜品失败，请稍后重试！");
        }catch (DuplicateKeyException e){
            ThrowUtils.throwIf(true, ErrorCode.DATA_EXIST_ERROR, "添加菜品失败，数据已存在！");
        }
        return dish.getId();
    }

    @Override
    public Boolean updateDish(Dish dish) {
        // 1. 判断是否存在
        boolean success = this.updateById(dish);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "更新食堂失败，请稍后重试！");
        return true;
    }

    @Override
    public Page<Dish> listDishsByPage(PageRequest pageRequest) {
        // 构建分页对象
        Page<Dish> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        // 构建查询条件
        QueryWrapper<Dish> queryWrapper = new QueryWrapper<>();
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            boolean isAsc = "ascend".equalsIgnoreCase(pageRequest.getSortOrder());
            queryWrapper.orderBy(true, isAsc, pageRequest.getSortField());
        }
        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public List<DishVO> listDishVOsByCanteenByList(Long canteenId) {
        return this.queryDishByCanteenId(canteenId)
                .stream()
                .filter(dish -> StatusConstant.ENABLE.equals(dish.getStatus()))
                .map(dish -> BeanUtil.copyProperties(dish, DishVO.class))
                .toList();
    }
}




