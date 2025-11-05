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
 * @TableName tb_school
 */
@TableName(value ="tb_school")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class School implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 7112853168356404121L;
}