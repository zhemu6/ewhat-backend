package com.lushihao.ewhatbackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 博文信息
 * @TableName tb_blog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogVO implements Serializable {

    /**
     * 菜品id
     */
    private Long dishId;

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 用户头像
     */
    private String icon;

    /**
     * 用户姓名
     */
    private String name;

    /**
     * 是否点赞
     */
    private Boolean isLike;

    /**
     * 标题
     */
    private String title;

    /**
     * 探店的照片，最多9张，多张以","隔开
     */
    private String images;

    /**
     * 探店的文字描述
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer liked;

    /**
     * 评论数量
     */
    private Integer comments;

    /**
     * 创建时间
     */
    private Date createTime;


    private static final long serialVersionUID = 1L;
}