package com.lushihao.ewhatbackend.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 封装游标查询返回结果
 * @author lushihao
 */
@Data
@Builder
public class ScrollResult {
    // 查询结果
    private List<?> list;
    // 上次的最小返回时间
    private Long minTime;
    // 偏移量
    private Integer offset;
}