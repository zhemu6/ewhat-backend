package com.lushihao.ewhatbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.model.entity.SeckillProduct;
import com.lushihao.ewhatbackend.service.SeckillProductService;
import com.lushihao.ewhatbackend.mapper.SeckillProductMapper;
import org.springframework.stereotype.Service;

/**
* @author lushihao
* @description 针对表【tb_seckill_product(秒杀商品表，与商品表是一对一关系)】的数据库操作Service实现
* @createDate 2025-11-09 20:28:19
*/
@Service
public class SeckillProductServiceImpl extends ServiceImpl<SeckillProductMapper, SeckillProduct>
    implements SeckillProductService{

}




