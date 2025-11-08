package com.lushihao.ewhatbackend.controller.user;

import com.lushihao.ewhatbackend.common.BaseResponse;
import com.lushihao.ewhatbackend.common.ResultUtils;
import com.lushihao.ewhatbackend.model.vo.UserVO;
import com.lushihao.ewhatbackend.service.FollowService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序端-关注相关服务服务
 * @author: lushihao
 * @version: 1.0
 * create:   2025-11-06   13:04
 */
@Slf4j
@RestController("userFollowController")
@RequestMapping("/user/follow")
public class FollowController {
    @Resource
    private FollowService followService;

    /**
     * 当前登陆用户关注或者取关followUserId
     * @param followUserId 关注用户id
     * @param isFollow 为True是关注 否则是取关
     * @return 操作成功
     */
    @PutMapping("{id}/{isFollow}")
    public BaseResponse<Boolean> follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow){
        return ResultUtils.success(followService.follow(followUserId,isFollow));
    }

    /**
     * 判断当前登录用户是否关注用户
     * @param followUserId 需要判断的被关注的用户id
     * @return 当前登录用户是否关注
     */
    @GetMapping("/or/not/{id}")
    public BaseResponse<Boolean> isFollow(@PathVariable("id") Long followUserId){
        return  ResultUtils.success(followService.isFollow(followUserId));
    }

    /**
     * 查询 当前登录用户和博主的共同关注
     * @param id 目标博主的userId
     * @return 共同关注用户
     */
    @GetMapping("/common/{id}")
    public BaseResponse<List<UserVO>> followCommons(@PathVariable("id") Long id){
        return  ResultUtils.success(followService.followCommons(id));
    }
}
