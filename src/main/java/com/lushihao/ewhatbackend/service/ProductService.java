package com.lushihao.ewhatbackend.service;

import com.lushihao.ewhatbackend.model.dto.ProductDTO;
import com.lushihao.ewhatbackend.model.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lushihao.ewhatbackend.model.vo.ProductVO;

import java.util.List;

/**
* @author lushihao
* @description 针对表【tb_product(商品表)】的数据库操作Service
* @createDate 2025-11-09 20:28:36
*/
public interface ProductService extends IService<Product> {

    Long addProduct(ProductDTO productDTO);

    List<Product> queryProductOfSchool(Long schoolId);

    List<ProductVO> queryProductVOOfSchool(Long schoolId);
}
