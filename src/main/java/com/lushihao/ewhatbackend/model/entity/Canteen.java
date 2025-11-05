package com.lushihao.ewhatbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学校信息
 * @author lushihao
 * @TableName tb_canteen
 */
@TableName(value ="tb_canteen")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Canteen implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 食堂名称
     */
    private String name;

    /**
     * 学校的id
     */
    private Long schoolId;

    /**
     * 图片，多个图片以','隔开
     */
    private String images;

    /**
     * 简介
     */
    private String description;

    /**
     * 地址
     */
    private String address;

    /**
     * 经度
     */
    private Double x;

    /**
     * 维度
     */
    private Double y;

    /**
     * 营业时间，例如 10:00-22:00
     */
    private String openHours;

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