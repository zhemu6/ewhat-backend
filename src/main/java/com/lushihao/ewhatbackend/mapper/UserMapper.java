package com.lushihao.ewhatbackend.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.lushihao.ewhatbackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* @author lushihao
* @description 针对表【tb_user(用户信息)】的数据库操作Mapper
* @createDate 2025-10-22 14:26:15
* @Entity com.lushihao.ewhatbackend.model.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    @InterceptorIgnore(tenantLine = "1")
    @Select("select * from tb_user where openid = #{openid} limit 1")
    User selectByOpenidNoTenant(String openid);

    @InterceptorIgnore(tenantLine = "1")
    @Select("select school_id from tb_user where id = #{userId}")
    Long selectSchoolIdByIdNoTenant(Long userId);

    @InterceptorIgnore(tenantLine = "1")
    @Update("update tb_user set school_id = #{schoolId} where id = #{userId}")
    int updateSchoolIdByIdNoTenant(Long userId, Long schoolId);

}




