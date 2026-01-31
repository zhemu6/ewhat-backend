package com.lushihao.ewhatbackend.controller.user;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.service.ProductOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 小程序端-商品秒杀服务
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
    public BaseResponse<Long> buyProduct(@PathVariable("id")Long productId){
        return ResultUtils.success(productOrderService.buyProduct(productId));
    }

    /**
     * 商品秒杀
     * @param productId 商品id
     * @return 订单id
     */
    @PostMapping("/seckill/{id}")
    public BaseResponse<Long> seckillProduct(@PathVariable("id")Long productId){
        return ResultUtils.success(productOrderService.seckillProduct(productId));
    }

}
