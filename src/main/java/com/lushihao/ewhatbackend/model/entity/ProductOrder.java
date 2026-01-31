package com.lushihao.ewhatbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品订单
 * @author lushihao
 * @TableName tb_product_order
 */
@TableName(value ="tb_product_order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrder implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 下单的用户id
     */
    private Long userId;

    /**
     * 购买的代金券id
     */
    private Long productId;

    /**
     * Tenant id.
     */
    private Long schoolId;

    /**
     * 订单状态，1：未支付；2：已支付；3：已核销；4：已取消；5：退款中；6：已退款
     */
    private Integer status;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 核销时间
     */
    private LocalDateTime useTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
