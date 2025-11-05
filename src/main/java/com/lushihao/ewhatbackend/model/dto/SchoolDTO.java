package com.lushihao.ewhatbackend.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 学校信息
 * @TableName tb_school
 */
@TableName(value ="tb_school")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolDTO implements Serializable {

    private static final long serialVersionUID = -7059982496164938634L;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 图片，多个图片以','隔开
     */
    private String images;

    /**
     * 地址
     */
    private String address;

    /**
     * 简介
     */
    private String description;

    /**
     * 状态 0:禁用，1:启用
     */
    private Integer status;


}