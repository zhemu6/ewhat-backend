package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.context.BaseContext;
import com.lushihao.ewhatbackend.model.entity.Follow;
import com.lushihao.ewhatbackend.model.vo.UserVO;
import com.lushihao.ewhatbackend.service.FollowService;
import com.lushihao.ewhatbackend.mapper.FollowMapper;
import com.lushihao.ewhatbackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author lushihao
* @description 针对表【tb_follow(关注)】的数据库操作Service实现
* @createDate 2025-11-06 13:02:42
*/
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
    implements FollowService{
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserService userService;

    /**
     * 当前登陆用户关注或者取关followUserId
     * @param followUserId 关注用户id
     * @param isFollow 为True是关注 否则是取关
     * @return
     */
    @Override
    public Boolean follow(Long followUserId, Boolean isFollow) {
        // 1.获得当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2.构建当前用户的关注Set的key
        String key = "follows:" + userId;
        // 3.判断是关注还是取关
        if(isFollow){
            // 关注
            Follow follow = Follow.builder().userId(userId).followUserId(followUserId).build();
            boolean isSuccess = this.save(follow);
            if(isSuccess){
                // 如果成功保存到数据库,将被关注用户的id放到当前用户的redis的set中
                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
            return true;
        }
        // 取关
        boolean isSuccess = this.remove(new QueryWrapper<Follow>().eq("user_id", userId).eq("follow_user_id", followUserId));
        if(isSuccess){
            // 将被关注用户的id从当前用户的redis的set中移除
            stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
        }
        return true;
    }
    /**
     * 判断当前登录用户是否关注用户
     * @param followUserId 需要判断的被关注的用户id
     * @return 当前登录用户是否关注
     */
    @Override
    public Boolean isFollow(Long followUserId) {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2. 从tb_follow中查询 count * from tb_follow where user_id = ? and follow_user_id = ?
        Long count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        return count>0;
    }

    /**
     * 查询 当前登录用户和博主的共同关注
     * @param id 目标博主的userId
     * @return 共同关注用户
     */
    @Override
    public List<UserVO> followCommons(Long id) {
        // 1.获取当前登录用户
        Long userId = BaseContext.getCurrentId();
        // 2.获取当前登录用户的关注的set的key
        String userKey = "follows:" + userId;
        // 3.获取目标博主userId的关注的set的key
        String targetUserKey = "follows:" + id;
        // 4.取交集 获取共同关注
        Set<String> intersectKey = stringRedisTemplate.opsForSet().intersect(userKey, targetUserKey);
        if(CollUtil.isEmpty(intersectKey)){
            // 无交集 返回空列表
            return Collections.emptyList();
        }
        // 5.共同关注的用户id
        List<Long> commonIds = intersectKey.stream().map(Long::valueOf).toList();
        // 6.转成UserVO
        return userService.listByIds(commonIds)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .toList();
    }
}




