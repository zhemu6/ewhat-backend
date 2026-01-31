package com.lushihao.ewhatbackend.controller.user;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.ProductDTO;
import com.lushihao.ewhatbackend.model.entity.Product;
import com.lushihao.ewhatbackend.model.vo.ProductVO;
import com.lushihao.ewhatbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端-商品服务
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-09   20:36
 */
@Slf4j
@RestController("userProductController")
@RequestMapping("/user/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 查询学校的兑换商品列表
     * @param schoolId 学校id
     * @return List<ProductVO>
     */
    @GetMapping("/list/{schoolId}")
    public BaseResponse<List<ProductVO>> queryProductVOOfSchool(@PathVariable("schoolId") Long schoolId){
        return ResultUtils.success(productService.queryProductVOOfSchool(schoolId));
    }

}
