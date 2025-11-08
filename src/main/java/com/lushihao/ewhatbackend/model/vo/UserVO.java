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
 * 用户信息 返回给前端
 *
 * @author lushihao
 * @TableName tb_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private Long id;

    /**
     * 姓名
     */
    private String name;


    /**
     * 性别
     */
    private String sex;


    /**
     * 头像
     */
    private String avatar;


    private static final long serialVersionUID = 1L;
}