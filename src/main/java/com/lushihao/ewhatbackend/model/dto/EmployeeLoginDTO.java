package com.lushihao.ewhatbackend.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeLoginDTO implements Serializable {

    private static final long serialVersionUID = -9138572840130329142L;

    private String username;

    private String password;

}
