package com.lushihao.ewhatbackend.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.lushihao.ewhatbackend.model.entity.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author lushihao
* @description 针对表【tb_employee(管理员信息)】的数据库操作Mapper
* @createDate 2025-10-22 14:25:59
* @Entity com.lushihao.ewhatbackend.model.entity.Employee
*/
public interface EmployeeMapper extends BaseMapper<Employee> {

    @InterceptorIgnore(tenantLine = "1")
    @Select("select * from tb_employee where username = #{username} limit 1")
    Employee selectByUsernameNoTenant(String username);

    @InterceptorIgnore(tenantLine = "1")
    @Select("select school_id from tb_employee where id = #{empId}")
    Long selectSchoolIdByIdNoTenant(Long empId);

    @InterceptorIgnore(tenantLine = "1")
    @Select("select role from tb_employee where id = #{empId}")
    Integer selectRoleByIdNoTenant(Long empId);

    @InterceptorIgnore(tenantLine = "1")
    @Select("select school_id, role from tb_employee where id = #{empId}")
    Employee selectSchoolAndRoleByIdNoTenant(Long empId);

}




