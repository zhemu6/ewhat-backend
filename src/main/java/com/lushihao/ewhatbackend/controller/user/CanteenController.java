package com.lushihao.ewhatbackend.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.lushihao.ewhatbackend.model.vo.CanteenVO;
import com.lushihao.ewhatbackend.service.CanteenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端-食堂管理
 *
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
     *
     * @param id 食堂id
     * @return 食堂视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<CanteenVO> queryCanteenById(@PathVariable("id") Long id) {
        Canteen canteen = canteenService.queryCanteenById(id);
        CanteenVO canteenVO = BeanUtil.copyProperties(canteen, CanteenVO.class);
        return ResultUtils.success(canteenVO);
    }


    /**
     * 查询某一个学校的食堂列表
     * @param schoolId 学校id
     * @param current 用于分页
     * @param x 经度
     * @param y 维度
     * @param distance 距离 查询多少范围内的
     * @return 按照距离最近返回
     */
    @GetMapping("/of/school")
    public BaseResponse<List<CanteenVO>> queryCanteenVOsBySchoolId(@RequestParam("schoolId") Long schoolId,
                                                                @RequestParam(value = "current", defaultValue = "1") Integer current,
                                                                @RequestParam(value = "x", required = false) Double x,
                                                                @RequestParam(value = "y", required = false) Double y,
                                                                   @RequestParam(value = "distance", required = false,defaultValue = "5000") Integer distance){
        List<CanteenVO> canteens = canteenService.queryCanteenVOsBySchoolId(schoolId,current,x,y,distance);
        return ResultUtils.success(canteens);
    }

}
