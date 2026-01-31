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
 * 秒杀商品表，与商品表是一对一关系
 * @author lushihao
 * @TableName tb_seckill_product
 */
@TableName(value ="tb_seckill_product")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillProduct implements Serializable {
    /**
     * 关联的商品的id
     */
    @TableId
    private Long productId;

    /**
     * Tenant id.
     */
    private Long schoolId;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 生效时间
     */
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    private LocalDateTime endTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
