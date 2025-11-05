package com.lushihao.ewhatbackend.exception;

import lombok.Getter;


/**
 * 错误码枚举类
 * 里面包含两个值 一个是状态code  一个是信息
 *
 * @author lushihao
 */
@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    USER_LOGIN_FAILED(40110, "用户登录失败"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    DATA_EXIST_ERROR(40401, "请求数据已存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
