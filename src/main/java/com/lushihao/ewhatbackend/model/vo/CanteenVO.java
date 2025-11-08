package com.lushihao.ewhatbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 学校信息
 * @author lushihao
 * @TableName tb_canteen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanteenVO implements Serializable {
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
     * 距离
     */
    private Double distance;

    private static final long serialVersionUID = 1L;
}