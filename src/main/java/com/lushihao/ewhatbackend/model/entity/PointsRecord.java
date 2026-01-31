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
 * 积分流水表
 * @author lushihao
 * @TableName tb_points_record
 */
@TableName(value ="tb_points_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * Tenant id.
     */
    private Long schoolId;

    /**
     * 积分变动数量（正数为获得，负数为消费）
     */
    private Long points;

    /**
     * 类型：1-签到获得，2-订单支付，3-订单退款，4-管理员调整
     */
    private Integer type;

    /**
     * 关联订单id（如果是订单相关）
     */
    private Long orderId;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
