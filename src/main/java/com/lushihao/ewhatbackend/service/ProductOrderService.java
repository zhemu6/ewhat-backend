package com.lushihao.ewhatbackend.service;

import com.lushihao.ewhatbackend.model.entity.ProductOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lushihao
* @description 针对表【tb_product_order(商品订单)】的数据库操作Service
* @createDate 2025-11-09 20:28:27
*/
public interface ProductOrderService extends IService<ProductOrder> {

    Long seckillProduct(Long productId);

    void createProductOrder(ProductOrder productOrder);

    Long buyProduct(Long productId);
}
