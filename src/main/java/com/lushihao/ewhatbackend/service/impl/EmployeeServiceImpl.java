package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.constant.StatusConstant;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.model.dto.EmployeeLoginDTO;
import com.lushihao.ewhatbackend.model.entity.Employee;
import com.lushihao.ewhatbackend.service.EmployeeService;
import com.lushihao.ewhatbackend.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author lushihao
* @description 针对表【tb_employee(管理员信息)】的数据库操作Service实现
* @createDate 2025-10-22 14:25:59
*/
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{
    /**
     * 管理员登录
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        // 1. 首先根据用户名查询用户
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();
        // 2. 用户不存在 说明账户异常
        QueryWrapper<Employee> wrapper = new QueryWrapper<Employee>().eq("username", username);
        Employee employee = this.getOne(wrapper);
        ThrowUtils.throwIf(employee==null, ErrorCode.NOT_FOUND_ERROR,"登录失败，账户不存在!");
        // 3. 对密码进行加密 与数据库中密码对比
        // 3.1 md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 3.2 加密后的密码和数据库比对
        ThrowUtils.throwIf(!password.equals(employee.getPassword()), ErrorCode.NOT_FOUND_ERROR,"登录失败，密码错误");
        // 4. 获取账户状态
        ThrowUtils.throwIf(ObjectUtil.equal(employee.getStatus(), StatusConstant.DISABLE),ErrorCode.NOT_FOUND_ERROR,"登录失败，账户被锁定");
        // 5. 返回实体对象
        return employee;
    }
}




