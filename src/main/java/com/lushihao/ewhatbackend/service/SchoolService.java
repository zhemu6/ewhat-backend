package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.model.dto.SchoolDTO;
import com.lushihao.ewhatbackend.model.entity.School;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.model.vo.SchoolVO;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_school(学校信息)】的数据库操作Service
* @createDate 2025-10-22 16:42:18
*/
public interface SchoolService extends IService<School> {

    School querySchoolById(Long id);

    Long save(SchoolDTO schoolDTO);

    Boolean updateSchool(School school);

    Page<School> listSchoolsByPage(PageRequest pageRequest);

    List<SchoolVO> listSchoolVOsByList();
}
