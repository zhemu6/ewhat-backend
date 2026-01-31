package com.lushihao.ewhatbackend.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 前端传入的商品
 *
 * @author lushihao
 * @TableName tb_product
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO implements Serializable {

    /**
     * 商品名称
     */
    private String name;

    /**
     * 学校id，关联tb_school.id
     */
    private Long schoolId;

    /**
     * 图片，多个图片以','隔开
     */
    private String images;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 优惠后的兑换所需积分
     */
    private Long payPoints;

    /**
     * 原本真实的兑换所需积分
     */
    private Long actualPoints;

    /**
     * 0,普通商品；1,秒杀商品
     */
    private Integer type;

    /**
     * 库存
     */
    @TableField(exist = false)
    private Integer stock;

    /**
     * 生效时间
     */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /**
     * 失效时间
     */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}