package com.lushihao.ewhatbackend.controller.admin;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.entity.ProductOrder;
import com.lushihao.ewhatbackend.service.ProductOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-订单核销管理
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-09   21:21
 */
@Slf4j
@RestController("adminProductOrderController")
@RequestMapping("/admin/product-order")
@RequiredArgsConstructor
public class ProductOrderController {

    private final ProductOrderService productOrderService;

    /**
     * 核销订单（使用兑换码）
     * @param exchangeCode 兑换码
     * @return 核销后的订单信息
     */
    @PostMapping("/use")
    public BaseResponse<ProductOrder> useOrder(@RequestParam("exchangeCode") String exchangeCode) {
        log.info("核销订单，兑换码：{}", exchangeCode);
        return ResultUtils.success(productOrderService.useOrder(exchangeCode));
    }

    /**
     * 分页查询订单列表
     * @param pageRequest 分页请求
     * @param status 订单状态（可选）
     * @return 订单分页
     */
    @GetMapping("/page")
    public BaseResponse<com.baomidou.mybatisplus.core.metadata.IPage<ProductOrder>> pageOrders(PageRequest pageRequest,
                                                       @RequestParam(value = "status", required = false) Integer status) {
        // 构建查询条件
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ProductOrder> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());
        var query = productOrderService.query();
        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");
        return ResultUtils.success(productOrderService.page(page, query));
    }
}
