package com.lushihao.ewhatbackend.model.dto;

import lombok.Data;

/**
 * @author lushihao
 */
@Data
public class BlogStatusUpdateRequest {
    
    /**
     * 博客ID
     */
    private Long id;
    
    /**
     * 状态 0:禁用，1:启用
     */
    private Integer status;
}