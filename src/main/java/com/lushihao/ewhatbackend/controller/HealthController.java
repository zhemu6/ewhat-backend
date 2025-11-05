package com.lushihao.ewhatbackend.controller;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * 检查项目是否通过
     *
     * @return BaseResponse
     */
    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}

