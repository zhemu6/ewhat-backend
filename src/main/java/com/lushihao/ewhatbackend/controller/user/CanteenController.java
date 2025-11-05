package com.lushihao.ewhatbackend.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.vo.CanteenVO;
import com.lushihao.ewhatbackend.service.CanteenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序端-食堂管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   18:12
 */
@Slf4j
@RestController("userCanteenController")
@RequestMapping("/user/canteen")
public class CanteenController {
    @Resource
    private CanteenService canteenService;
    
    /**
     * 根据id查询食堂
     * @param id 食堂id
     * @return 食堂视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<CanteenVO> queryCanteenById(@PathVariable("id") Long id){
        Canteen canteen =  canteenService.queryCanteenById(id);
        CanteenVO canteenVO = BeanUtil.copyProperties(canteen, CanteenVO.class);
        return ResultUtils.success(canteenVO);
    }

    /**
     * 查询某一学校的食堂列表
     * @return 查询结果 list集合
     */
    @GetMapping("/listBySchool/{id}")
    public BaseResponse<List<CanteenVO>> listCanteenVOsBySchool(@PathVariable("id") Long schoolId){
        List<CanteenVO> canteens  = canteenService.listCanteenVOsBySchoolByList(schoolId);
        return ResultUtils.success(canteens );
    }
    
}
