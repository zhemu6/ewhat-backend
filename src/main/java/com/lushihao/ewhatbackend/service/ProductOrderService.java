package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.model.entity.ProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.model.vo.ProductOrderVO;

/**
* @author lushihao
* @description 针对表【tb_product_order(商品订单)】的数据库操作Service
* @createDate 2025-11-09 20:28:27
*/
public interface ProductOrderService extends IService<ProductOrder> {

    Long seckillProduct(Long productId);

    void createProductOrder(ProductOrder productOrder);

    Long buyProduct(Long productId);

    /**
     * 支付订单（使用积分）
     * @param orderId 订单id
     * @return 支付后的订单
     */
    ProductOrder payOrder(Long orderId);

    /**
     * 核销订单
     * @param exchangeCode 兑换码
     * @return 核销后的订单
     */
    ProductOrder useOrder(String exchangeCode);

    /**
     * 取消订单
     * @param orderId 订单id
     * @return 是否取消成功
     */
    Boolean cancelOrder(Long orderId);

    /**
     * 查询用户的订单列表
     * @param status 订单状态（可选）
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 订单分页
     */
    com.baomidou.mybatisplus.core.metadata.IPage<ProductOrderVO> queryUserOrders(Integer status, Long current, Long pageSize);

    /**
     * 查询订单详情
     * @param orderId 订单id
     * @return 订单详情VO
     */
    ProductOrderVO queryOrderDetail(Long orderId);
}
