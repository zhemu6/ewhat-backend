package com.lushihao.ewhatbackend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 博文评论前端传入
 *
 * @author lushihao
 * @TableName tb_blog_comments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCommentDTO implements Serializable {

    /**
     * 博文id
     */
    private Long blogId;

    /**
     * 关联的1级评论id，如果是一级评论，则值为0
     */
    private Long parentId;

    /**
     * 回复的评论id
     */
    private Long answerId;

    /**
     * 回复的内容
     */
    private String content;


    private static final long serialVersionUID = 1L;
}