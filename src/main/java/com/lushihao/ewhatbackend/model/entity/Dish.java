package com.lushihao.ewhatbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 餐品信息
 * @TableName tb_dish
 */
@TableName(value ="tb_dish")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dish implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 餐品名称
     */
    private String name;

    /**
     * 食堂id，关联tb_canteen.id
     */
    private Long canteenId;

    /**
     * 图片，多个图片以','隔开
     */
    private String images;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 原价（用于显示折扣）
     */
    private BigDecimal originalPrice;

    /**
     * 简介
     */
    private String description;

    /**
     * 售卖位置（如：一楼2号窗口）
     */
    private String location;

    /**
     * 排序字段，数字越小越靠前
     */
    private Integer sort;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 评分人数
     */
    private Integer ratingCount;

    /**
     * 标签，多个以逗号分隔（如：辣,推荐,新品）
     */
    private String tags;

    /**
     * 是否推荐 0:否，1:是
     */
    private Integer isRecommend;

    /**
     * 状态 0:禁用，1:启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}