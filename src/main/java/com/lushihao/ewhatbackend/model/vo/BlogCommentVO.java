package com.lushihao.ewhatbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.io.Serializable;
import java.util.List;

/**
 * 传给前端的
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-09   18:49
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogCommentVO implements Serializable {

    private Long id;

    /**
     * 用户VO
     */
    private UserVO user;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer liked;

    /**
     * 是否点赞
     */
    private Boolean isLike;

    /**
     * 评论时间
     */
    private Date createTime;

    /**
     * 父评论id
     */
    private Long parentId;

    /**
     * 回复的id
     */
    private Long answerId;

    /**
     * 回答的用户
     */
    private UserVO answerUser;

    /**
     * 子评论
     */
    private List<BlogCommentVO> children;

    private static final long serialVersionUID = 1L;
}
