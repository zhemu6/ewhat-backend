package com.lushihao.ewhatbackend.interceptor;

import com.lushihao.ewhatbackend.config.JwtProperties;
import com.lushihao.ewhatbackend.constant.JwtClaimsConstant;
import com.lushihao.ewhatbackend.constant.RoleConstant;
import com.lushihao.ewhatbackend.context.BaseContext;
import com.lushihao.ewhatbackend.context.TenantContextHolder;
import com.lushihao.ewhatbackend.mapper.EmployeeMapper;
import com.lushihao.ewhatbackend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


/**
 * jwt令牌校验的拦截器
 * @author lushihao
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final EmployeeMapper employeeMapper;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
            BaseContext.setCurrentId(empId);

            Integer role;
            Object roleObj = claims.get(JwtClaimsConstant.ROLE);
            if (roleObj != null) {
                role = Integer.valueOf(roleObj.toString());
            } else {
                role = employeeMapper.selectRoleByIdNoTenant(empId);
            }
            TenantContextHolder.setRole(role);

            if (RoleConstant.SUPER_ADMIN == role) {
                // Super admin: require explicit school context for writes.
                String schoolIdHeader = request.getHeader("X-School-Id");
                if (schoolIdHeader != null && !schoolIdHeader.isBlank()) {
                    TenantContextHolder.setSchoolId(Long.valueOf(schoolIdHeader));
                    TenantContextHolder.setTenantBypass(false);
                } else {
                    TenantContextHolder.setTenantBypass(true);
                    if (!"GET".equalsIgnoreCase(request.getMethod())) {
                        response.setStatus(400);
                        return false;
                    }
                }
            } else {
                Long schoolId;
                Object schoolObj = claims.get(JwtClaimsConstant.SCHOOL_ID);
                if (schoolObj != null) {
                    schoolId = Long.valueOf(schoolObj.toString());
                } else {
                    schoolId = employeeMapper.selectSchoolIdByIdNoTenant(empId);
                }
                TenantContextHolder.setSchoolId(schoolId);
                TenantContextHolder.setTenantBypass(false);
            }

            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            log.error("请求失败，用户未登录");
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
        TenantContextHolder.clear();
    }
}
