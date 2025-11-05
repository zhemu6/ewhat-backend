package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.constant.WeChatProperties;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.UserLoginDTO;
import com.lushihao.ewhatbackend.model.entity.User;
import com.lushihao.ewhatbackend.service.UserService;
import com.lushihao.ewhatbackend.mapper.UserMapper;
import com.lushihao.ewhatbackend.utils.HttpClientUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
* @author lushihao
* @description 针对表【tb_user(用户信息)】的数据库操作Service实现
* @createDate 2025-10-22 14:26:15
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{


    @Resource
    private WeChatProperties weChatProperties;

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";


    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // 根据
        String openid = getOpenId(userLoginDTO.getCode());

        // 对Openid去做校验
        ThrowUtils.throwIf(openid==null, ErrorCode.USER_LOGIN_FAILED,"用户登录失败，openid为空");
        // 通过openid获得登录用户
        QueryWrapper<User> wrapper = new QueryWrapper<User>().eq("openid", openid);
        User user = this.getOne(wrapper);
        // 如果这个用户是新用户 存起来Openid 自动完成注册实现 返回用户对象
        if (user==null){
            user = User.builder().openid(openid).createTime(DateTime.now()).build();
            this.save(user);
        }
        // 返回这个对象
        return user;
    }
    private String getOpenId(String code){
        // 调用微信接口服务 获取当前用户的Openid https://api.weixin.qq.com/sns/jscode2session?appid=wxfc910c6008950980&secret=8ce6f32e638001826ac67ea7c0a01dcb&js_code=0c3tg6000e41CU1NVH200zrGZ02tg60E&grant_type=authorization_code
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信接口返回：{}", json);
        JSONObject jsonObject = JSON.parseObject(json);
        // 拿到openid
        return jsonObject.getString("openid");
    }
}




