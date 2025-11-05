package com.lushihao.ewhatbackend.service;

import com.lushihao.ewhatbackend.model.dto.EmployeeLoginDTO;
import com.lushihao.ewhatbackend.model.entity.Employee;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author lushihao
* @description 针对表【tb_employee(管理员信息)】的数据库操作Service
* @createDate 2025-10-22 14:25:59
*/
public interface EmployeeService extends IService<Employee> {

    Employee login(EmployeeLoginDTO employeeLoginDTO);
}
