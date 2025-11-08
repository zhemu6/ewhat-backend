package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.constant.WeChatProperties;
import com.lushihao.ewhatbackend.context.BaseContext;
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
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lushihao.ewhatbackend.constant.RedisConstants.USER_SIGN_KEY;

/**
 * @author lushihao
 * @description 针对表【tb_user(用户信息)】的数据库操作Service实现
 * @createDate 2025-10-22 14:26:15
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Resource
    private WeChatProperties weChatProperties;

    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // 根据
        String code = userLoginDTO.getCode();
        String openid = getOpenId(userLoginDTO.getCode());

        // 对Openid去做校验
        ThrowUtils.throwIf(openid == null, ErrorCode.USER_LOGIN_FAILED, "用户登录失败，openid为空");
        // 通过openid获得登录用户
        QueryWrapper<User> wrapper = new QueryWrapper<User>().eq("openid", openid);
        User user = this.getOne(wrapper);
        // 如果这个用户是新用户 存起来Openid 自动完成注册实现 返回用户对象
        if (user == null) {
            user = User.builder().openid(openid).createTime(DateTime.now()).build();
            this.save(user);
        }
        // 返回这个对象
        return user;
    }

    /**
     * 用户签到功能
     *
     * @return 签到成功
     */
    @Override
    public Boolean sign() {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2.获取当前日期
        LocalDateTime now = LocalDateTime.now();
        // 3.构建key sign:1:202510
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.获取当前是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.写入Redis setbit key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return true;
    }

    @Override
    public Integer signCount() {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2.获取当前日期
        LocalDateTime now = LocalDateTime.now();
        // 3.构建key sign:1:202510
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue()
                .bitField(key,
                        BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
                );
        if(CollUtil.isEmpty(result)){
            return 0;
        }
        Long num = result.get(0);
        if(num==null||num==0){
            return 0;
        }
        // 6.循环遍历
        int count = 0;
        while(true){
            // 6.1.让这个数字与1做与运算，得到数字的最后一个bit位  // 判断这个bit位是否为0
            if ((num & 1) == 0) {
                // 如果为0，说明未签到，结束
                break;
            }else {
                // 如果不为0，说明已签到，计数器+1
                count++;
            }
            // 把数字右移一位，抛弃最后一个bit位，继续下一个bit位
            num >>>= 1;
        }
        return count;
    }

    private String getOpenId(String code) {
        // 调用微信接口服务 获取当前用户的Openid https://api.weixin.qq.com/sns/jscode2session?appid=wxfc910c6008950980&secret=8ce6f32e638001826ac67ea7c0a01dcb&js_code=0c3tg6000e41CU1NVH200zrGZ02tg60E&grant_type=authorization_code
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);
        log.info("微信接口返回：{}", json);
        JSONObject jsonObject = JSON.parseObject(json);
        // 拿到openid
        return jsonObject.getString("openid");
    }
}




