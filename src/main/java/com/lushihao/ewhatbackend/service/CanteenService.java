package com.lushihao.ewhatbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lushihao.ewhatbackend.common.PageRequest;
import com.lushihao.ewhatbackend.model.dto.CanteenDTO;
import com.lushihao.ewhatbackend.model.entity.Canteen;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.model.vo.CanteenVO;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_canteen(学校信息)】的数据库操作Service
* @createDate 2025-10-22 17:56:01
*/
public interface CanteenService extends IService<Canteen> {

    Canteen queryCanteenById(Long id);

    Long save(CanteenDTO canteenDTO);

    Boolean updateCanteen(Canteen canteen);

    Page<Canteen> listCanteensByPage(PageRequest pageRequest);

    List<Canteen> queryCanteenBySchoolId(Long schoolId);

    List<CanteenVO> listCanteenVOsBySchoolByList(Long schoolId);
}
