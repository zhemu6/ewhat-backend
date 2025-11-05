package com.lushihao.ewhatbackend.controller.user;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.config.JwtProperties;
import com.lushihao.ewhatbackend.constant.JwtClaimsConstant;
import com.lushihao.ewhatbackend.model.dto.UserLoginDTO;
import com.lushihao.ewhatbackend.model.entity.User;
import com.lushihao.ewhatbackend.model.vo.UserLoginVO;
import com.lushihao.ewhatbackend.service.UserService;
import com.lushihao.ewhatbackend.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
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
 * 微信小程序用户相关操作
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   14:28
 */
@Slf4j
@RestController
@RequestMapping("/user/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private JwtProperties jwtProperties;

    @PostMapping("/login")
    public BaseResponse<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录，登录信息为：{}", userLoginDTO);
        // 获得一个登录用户
        User user = userService.login(userLoginDTO);

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        UserLoginVO userLoginVO = UserLoginVO.builder().id(user.getId()).openid(user.getOpenid()).token(token).build();

        return ResultUtils.success(userLoginVO);
    }
}
