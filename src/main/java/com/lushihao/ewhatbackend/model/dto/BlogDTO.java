package com.lushihao.ewhatbackend.model.dto;

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
 * @author lushihao
 * @TableName tb_blog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogDTO implements Serializable {
    /**
     * 菜品id
     */
    private Long dishId;

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



    private static final long serialVersionUID = 1L;
}