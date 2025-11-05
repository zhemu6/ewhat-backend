package com.lushihao.ewhatbackend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * C端用户登录
 *
 * @author lushihao
 */
@Data
public class UserLoginDTO implements Serializable {

    private static final long serialVersionUID = -4200159623375353529L;

    private String code;

}
