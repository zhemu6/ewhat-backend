package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.model.dto.DishDTO;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.entity.Dish;
import com.lushihao.ewhatbackend.model.vo.DishVO;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_dish(餐品信息)】的数据库操作Service
* @createDate 2025-11-05 16:35:31
*/
public interface DishService extends IService<Dish> {

    Dish queryDishById(Long id);

    List<Dish> queryDishByCanteenId(Long canteenId);

    Long save(DishDTO dishDTO);

    Boolean updateDish(Dish dish);

    Page<Dish> listDishsByPage(PageRequest pageRequest);


    List<DishVO> listDishVOsByCanteenByList(Long canteenId);
}
