package com.lushihao.ewhatbackend.controller.admin;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.dto.ProductDTO;
import com.lushihao.ewhatbackend.model.entity.Product;
import com.lushihao.ewhatbackend.service.ProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-商品服务
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-09   20:36
 */
@Slf4j
@RestController("adminProductController")
@RequestMapping("/admin/product")
public class ProductController {

    @Resource
    private ProductService productService;

    /**
     * 新增商品
     *
     * @param productDTO 前端传入商品DTO
     * @return 添加后的商品id
     */
    @PostMapping
    public BaseResponse<Long> addProduct(@RequestBody ProductDTO productDTO) {
        return ResultUtils.success(productService.addProduct(productDTO));
    }

    /**
     * 查询学校的兑换商品列表
     * @param schoolId 学校id
     * @return List<Product>
     */
    @GetMapping("/list/{schoolId}")
    public BaseResponse<List<Product>> queryProductOfSchool(@PathVariable("schoolId") Long schoolId){
        return ResultUtils.success(productService.queryProductOfSchool(schoolId));
    }

}
