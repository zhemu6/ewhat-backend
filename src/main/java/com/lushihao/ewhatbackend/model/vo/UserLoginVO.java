package com.lushihao.ewhatbackend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author lushihao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO implements Serializable {


    private static final long serialVersionUID = -8757102502173573412L;
    private Long id;
    private String openid;
    private String token;
}
