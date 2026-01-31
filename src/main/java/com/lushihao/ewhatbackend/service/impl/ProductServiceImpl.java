package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.mapper.SchoolMapper;
import com.lushihao.ewhatbackend.mapper.SeckillProductMapper;
import com.lushihao.ewhatbackend.model.dto.ProductDTO;
import com.lushihao.ewhatbackend.model.entity.Product;
import com.lushihao.ewhatbackend.model.entity.School;
import com.lushihao.ewhatbackend.model.entity.SeckillProduct;
import com.lushihao.ewhatbackend.model.vo.ProductVO;
import com.lushihao.ewhatbackend.service.ProductService;
import com.lushihao.ewhatbackend.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.lushihao.ewhatbackend.constant.RedisConstants.SECKILL_STOCK_KEY;

/**
 * @author lushihao
 * @description 针对表【tb_product(商品表)】的数据库操作Service实现
 * @createDate 2025-11-09 20:28:36
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
        implements ProductService {

    private final SeckillProductMapper seckillProductMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final SchoolMapper schoolMapper;
    /**
     * 新增商品
     *
     * @param productDTO 前端传入商品DTO
     * @return 添加后的商品id
     */
    @Override
    @Transactional
    public Long addProduct(ProductDTO productDTO) {
        Long schoolId = productDTO.getSchoolId();
        School school = schoolMapper.selectById(schoolId);
        ThrowUtils.throwIf(school==null, ErrorCode.NOT_FOUND_ERROR,"选择的学校不存在");
        Integer type = productDTO.getType();
        Integer stock = productDTO.getStock();
        LocalDateTime beginTime = productDTO.getBeginTime();
        LocalDateTime endTime = productDTO.getEndTime();
        Product product = BeanUtil.copyProperties(productDTO, Product.class);
        this.save(product);
        // 秒杀商品
        if (type == 1) {
            // 保存到seckill_product中
            SeckillProduct seckillProduct = SeckillProduct.builder()
                    .productId(product.getId())
                    .stock(stock)
                    .beginTime(beginTime)
                    .endTime(endTime)
                    .build();
            seckillProductMapper.insert(seckillProduct);
            // 缓存秒杀商品的库存到redis中 其中key形式为 seckill:stock:product_id value是库存值
            String key = SECKILL_STOCK_KEY + product.getId();
            stringRedisTemplate.opsForValue().set(key,seckillProduct.getStock().toString());
        }
        // 返回商品id
        return product.getId();
    }

    @Override
    public List<Product> queryProductOfSchool(Long schoolId) {
        // 查询优惠券信息
        return getBaseMapper().queryProductOfSchool(schoolId);
    }

    @Override
    public List<ProductVO> queryProductVOOfSchool(Long schoolId) {
        List<Product> products = queryProductOfSchool(schoolId);
        return BeanUtil.copyToList(products, ProductVO.class);
    }
}




