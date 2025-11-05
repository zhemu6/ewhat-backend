package com.lushihao.ewhatbackend.service;

import com.lushihao.ewhatbackend.model.dto.UserLoginDTO;
import com.lushihao.ewhatbackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lushihao
* @description 针对表【tb_user(用户信息)】的数据库操作Service
* @createDate 2025-10-22 14:26:15
*/
public interface UserService extends IService<User> {

    User login(UserLoginDTO userLoginDTO);
}
