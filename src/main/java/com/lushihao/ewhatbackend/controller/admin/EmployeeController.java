package com.lushihao.ewhatbackend.controller.admin;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.config.JwtProperties;
import com.lushihao.ewhatbackend.constant.JwtClaimsConstant;
import com.lushihao.ewhatbackend.model.dto.EmployeeLoginDTO;
import com.lushihao.ewhatbackend.model.entity.Employee;
import com.lushihao.ewhatbackend.model.vo.EmployeeLoginVO;
import com.lushihao.ewhatbackend.service.EmployeeService;
import com.lushihao.ewhatbackend.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员相关接口
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   15:55
 */
@Slf4j
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;
    @Resource
    private JwtProperties jwtProperties;

    /**
     * 管理员登录
     * @param employeeLoginDTO 管理员登录DTO
     * @return 管理员登录VO类
     */
    @PostMapping("login")
    public BaseResponse<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("管理员登录：{}", employeeLoginDTO);
        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return ResultUtils.success(employeeLoginVO);
    }

}
