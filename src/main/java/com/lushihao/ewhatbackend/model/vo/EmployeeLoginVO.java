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
public class EmployeeLoginVO implements Serializable {

    private static final long serialVersionUID = 5045080549375830080L;

    private Long id;

    private String userName;

    private String name;

    private String token;

}
