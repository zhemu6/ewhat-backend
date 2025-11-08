package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.model.entity.Follow;
import com.lushihao.ewhatbackend.model.vo.UserVO;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_follow(关注)】的数据库操作Service
* @createDate 2025-11-06 13:02:42
*/
public interface FollowService extends IService<Follow> {

    Boolean follow(Long followUserId, Boolean isFollow);

    Boolean isFollow(Long followUserId);

    List<UserVO> followCommons(Long id);
}
