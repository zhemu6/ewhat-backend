package com.lushihao.ewhatbackend.mapper;

import com.lushihao.ewhatbackend.model.entity.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author lushihao
 * @description 针对表【tb_product(商品表)】的数据库操作Mapper
 * @createDate 2025-11-09 20:28:36
 * @Entity com.lushihao.ewhatbackend.model.entity.Product
 */
public interface ProductMapper extends BaseMapper<Product> {

    List<Product> queryProductOfSchool(@Param("schoolId") Long schoolId);

}




