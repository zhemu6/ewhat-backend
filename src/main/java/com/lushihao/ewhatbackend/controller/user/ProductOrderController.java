package com.lushihao.ewhatbackend.controller.user;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.entity.ProductOrder;
import com.lushihao.ewhatbackend.model.vo.ProductOrderVO;
import com.lushihao.ewhatbackend.service.ProductOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序端-商品订单服务
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-09   21:21
 */
@Slf4j
@RestController("userProductOrderController")
@RequestMapping("/user/product-order")
@RequiredArgsConstructor
public class ProductOrderController {

    private final ProductOrderService productOrderService;

    /**
     * 普通商品下单
     * @param productId 商品id
     * @return 订单id
     */
    @PostMapping("/{id}")
    public BaseResponse<Long> buyProduct(@PathVariable("id") Long productId) {
        return ResultUtils.success(productOrderService.buyProduct(productId));
    }

    /**
     * 商品秒杀
     * @param productId 商品id
     * @return 订单id
     */
    @PostMapping("/seckill/{id}")
    public BaseResponse<Long> seckillProduct(@PathVariable("id") Long productId) {
        return ResultUtils.success(productOrderService.seckillProduct(productId));
    }

    /**
     * 支付订单（使用积分）
     * @param orderId 订单id
     * @return 支付后的订单信息
     */
    @PostMapping("/pay/{id}")
    public BaseResponse<ProductOrder> payOrder(@PathVariable("id") Long orderId) {
        return ResultUtils.success(productOrderService.payOrder(orderId));
    }

    /**
     * 取消订单
     * @param orderId 订单id
     * @return 是否取消成功
     */
    @PostMapping("/cancel/{id}")
    public BaseResponse<Boolean> cancelOrder(@PathVariable("id") Long orderId) {
        return ResultUtils.success(productOrderService.cancelOrder(orderId));
    }

    /**
     * 查询我的订单列表
     * @param status 订单状态（可选）
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 订单分页
     */
    @GetMapping("/list")
    public BaseResponse<com.baomidou.mybatisplus.core.metadata.IPage<ProductOrderVO>> queryMyOrders(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "current", defaultValue = "1") Long current,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return ResultUtils.success(productOrderService.queryUserOrders(status, current, pageSize));
    }

    /**
     * 查询订单详情
     * @param orderId 订单id
     * @return 订单详情
     */
    @GetMapping("/detail/{id}")
    public BaseResponse<ProductOrderVO> queryOrderDetail(@PathVariable("id") Long orderId) {
        return ResultUtils.success(productOrderService.queryOrderDetail(orderId));
    }
}
