package com.lushihao.ewhatbackend.controller.user;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.config.JwtProperties;
import com.lushihao.ewhatbackend.constant.JwtClaimsConstant;
import com.lushihao.ewhatbackend.context.TenantContextHolder;
import com.lushihao.ewhatbackend.model.dto.UserLoginDTO;
import com.lushihao.ewhatbackend.model.entity.User;
import com.lushihao.ewhatbackend.model.vo.UserLoginVO;
import com.lushihao.ewhatbackend.service.UserService;
import com.lushihao.ewhatbackend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 小程序端-用户相关操作
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-10-22   14:28
 */
@Slf4j
@RestController
@RequestMapping("/user/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final JwtProperties jwtProperties;

    /**
     * 用户登陆
     *
     * @param userLoginDTO 存储UUID
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        try {
            log.info("用户登录");
            // 获得一个登录用户
            User user = userService.login(userLoginDTO);

            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.USER_ID, user.getId());
            String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
            UserLoginVO userLoginVO = UserLoginVO.builder().id(user.getId()).openid(user.getOpenid()).token(token).build();

            return ResultUtils.success(userLoginVO);
        } finally {
            // login endpoint is excluded from interceptor; must clear manually
            TenantContextHolder.clear();
        }
    }

    /**
     * 更新当前用户绑定的学校。
     * 历史数据不迁移：老数据仍属于原 school_id，新学校下默认不可见。
     */
    @PutMapping("/school")
    public BaseResponse<Boolean> updateSchool(@RequestParam("schoolId") Long schoolId) {
        return ResultUtils.success(userService.updateSchoolId(schoolId));
    }

    /**
     * 用户签到功能
     *
     * @return 签到成功
     */
    @PostMapping("/sign")
    public BaseResponse<Boolean> sign() {
        Boolean isSuccess = userService.sign();
        return ResultUtils.success(isSuccess);
    }

    /**
     * 统计当前用户当前月份连续签到数
     *
     * @return 签到天数
     */
    @GetMapping("/sign/count")
    public BaseResponse<Integer> signCount() {
        return ResultUtils.success(userService.signCount());
    }
}
