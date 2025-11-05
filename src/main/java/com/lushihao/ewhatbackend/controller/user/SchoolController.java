package com.lushihao.ewhatbackend.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.entity.School;
import com.lushihao.ewhatbackend.model.vo.SchoolVO;
import com.lushihao.ewhatbackend.service.SchoolService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序端-学校管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   17:35
 */
@Slf4j
@RestController("userSchoolController")
@RequestMapping("/user/school")
public class SchoolController {

    @Resource
    private SchoolService schoolService;

    /**
     * 根据id查询学校
     * @param id 学校id
     * @return 学校视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<SchoolVO> querySchoolById(@PathVariable("id") Long id){
        School school =  schoolService.querySchoolById(id);
        SchoolVO schoolVO = BeanUtil.copyProperties(school, SchoolVO.class);
        return ResultUtils.success(schoolVO);
    }

    /**
     * 查询学校列表
     * @return 查询结果 list集合
     */
    @GetMapping("/list")
    public BaseResponse<List<SchoolVO>> listSchoolVOs( ){
        List<SchoolVO> schools  = schoolService.listSchoolVOsByList();
        return ResultUtils.success(schools );
    }

}
