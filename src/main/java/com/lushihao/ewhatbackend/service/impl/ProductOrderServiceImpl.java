package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.context.BaseContext;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.mapper.ProductOrderMapper;
import com.lushihao.ewhatbackend.model.entity.Product;
import com.lushihao.ewhatbackend.model.entity.ProductOrder;
import com.lushihao.ewhatbackend.model.entity.SeckillProduct;
import com.lushihao.ewhatbackend.service.ProductOrderService;
import com.lushihao.ewhatbackend.service.ProductService;
import com.lushihao.ewhatbackend.service.SeckillProductService;
import com.lushihao.ewhatbackend.utils.RedisIdWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author lushihao
 * @description 针对表【tb_product_order(商品订单)】的数据库操作Service实现
 * @createDate 2025-11-09 20:28:27
 */
@Slf4j
@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder>
        implements ProductOrderService {
    @Resource
    private SeckillProductService seckillProductService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private ProductService productService;
    // 创建秒杀订单异步处理线程池
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
    // 秒杀的lua脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }


    // 当前类初始化完毕后 立刻执行
    @PostConstruct
    private void init() {
        // 创建消费者组（如果不存在）
        try {
            stringRedisTemplate.opsForStream().createGroup("stream.orders", "g1");
        } catch (Exception e) {
            // 消费者组已存在，忽略异常
            log.debug("消费者组已存在或创建失败: {}", e.getMessage());
        }
        SECKILL_ORDER_EXECUTOR.submit(new ProductOrderHandler());
    }

    @PreDestroy
    private void destroy() {
        SECKILL_ORDER_EXECUTOR.shutdown();
        try {
            if (!SECKILL_ORDER_EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                SECKILL_ORDER_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            SECKILL_ORDER_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // 订单处理器
    private class ProductOrderHandler implements Runnable {
        @Override
        public void run() {
            // 死循环 一直重复处理
            while (true) {
                try {
                    // 1.获取消息队列中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
                    // 「消费者组 g1 的消费者 c1 从 stream.orders 流里读取1条消息（最多等2秒）。」
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create("stream.orders", ReadOffset.lastConsumed())
                    );
                    // 2.判断消息
                    if (CollUtil.isEmpty(list)) {
                        continue;
                    }
                    // 3.解析数据 一次获取一条 所以非空 那0上一定有值
                    MapRecord<String, Object, Object> entries = list.get(0);
                    // 'userId',userId,'productId',productId,'id',orderId)
                    Map<Object, Object> value = entries.getValue();
                    ProductOrder productOrder = BeanUtil.fillBeanWithMap(value, new ProductOrder(), true);
                    // 创建订单
                    createProductOrder(productOrder);
                    // 消费者发送确认
                    stringRedisTemplate.opsForStream().acknowledge("stream.orders", "g1", entries.getId());
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                    // 处理异常消息
                    handlePendingList();
                }
            }
        }
    }

    /**
     * 异常消息补偿 处理pending-list（未确认消息）
     */
    private void handlePendingList() {
        while (true) {
            try {
                // 1.获取pending-list中的订单信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
                // 「消费者组 g1 的消费者 c1 从 stream.orders 流里读取1条消息。从 pending-list 的最早消息开始读取。」
                List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                        Consumer.from("g1", "c1"),
                        StreamReadOptions.empty().count(1),
                        StreamOffset.create("stream.orders", ReadOffset.from("0"))
                );
                // 2.判断订单信息是否为空 没有消息说明 pending-list 为空，直接退出循环。
                if (list == null || list.isEmpty()) {
                    break;
                }
                // 3.解析数据
                MapRecord<String, Object, Object> record = list.get(0);
                Map<Object, Object> value = record.getValue();
                ProductOrder productOrder = BeanUtil.fillBeanWithMap(value, new ProductOrder(), true);
                // 4.创建订单
                createProductOrder(productOrder);
                // 5. 确认消息
                stringRedisTemplate.opsForStream().acknowledge("stream.orders", "g1", record.getId());
            } catch (Exception e) {
                log.error("处理pending订单异常", e);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("线程被中断", ie);
                    break; // 中断时退出循环
                }
            }
        }
    }


    /**
     * 商品秒杀
     *
     * @param productId 商品id
     * @return 订单id
     */
    @Override
    public Long seckillProduct(Long productId) {
        // 1.查询商品信息
        SeckillProduct seckillProduct = seckillProductService.getById(productId);
        ThrowUtils.throwIf(seckillProduct == null, ErrorCode.NOT_FOUND_ERROR, "秒杀商品不存在!");
        // 2.时间校验
        ThrowUtils.throwIf(seckillProduct.getBeginTime().isAfter(LocalDateTime.now()), ErrorCode.OPERATION_ERROR, "秒杀尚未开始!");
        ThrowUtils.throwIf(seckillProduct.getEndTime().isBefore(LocalDateTime.now()), ErrorCode.OPERATION_ERROR, "秒杀已经结束!");
        //  利用全局唯一id生成器获得订单id
        long orderId = redisIdWorker.nextId("order");
        // 3.判断库存是否充足、以及用户是否下单
        Long userId = BaseContext.getCurrentId();
        // 3.1 使用Lua脚本 判断库存是否充足以及用户是否下单
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                productId.toString(), userId.toString(), String.valueOf(orderId));
        // 库存不足返回1 用户已经下过单了 返回2 成功返回0
        int r = result.intValue();
        if (r != 0) {
            ThrowUtils.throwIf(r == 2, ErrorCode.DATA_EXIST_ERROR, "请勿重复下单！");
            ThrowUtils.throwIf(r == 1, ErrorCode.NOT_FOUND_ERROR, "库存不足！");
        }
        // 此时在redis中完成了库存扣减 并将userId添加到set中 并且发送了一条记录 'userId',userId,'productId',productId,'id',orderId
        return orderId;
    }

    /**
     * 创建订单
     *
     * @param productOrder
     */
    @Override
    @Transactional
    public void createProductOrder(ProductOrder productOrder) {
        Long userId = productOrder.getUserId();
        // 避免重复下单
        Long count = this.query().eq("user_id", userId).eq("product_id", productOrder.getProductId()).count();
        ThrowUtils.throwIf(count > 0, ErrorCode.DATA_EXIST_ERROR, "请勿重复下单!");
        boolean success = seckillProductService
                .update()
                // 库存扣减
                .setSql("stock = stock - 1")
                .eq("product_id", productOrder.getProductId())
                // 乐观锁 防止超卖
                .gt("stock", 0)
                .update();
        ThrowUtils.throwIf(!success, ErrorCode.NOT_FOUND_ERROR, "优惠券已抢光！");
        // 保存订单
        this.save(productOrder);
    }

    /**
     * 普通商品下单
     *
     * @param productId 商品id
     * @return 订单id
     */
    @Override
    public Long buyProduct(Long productId) {
        // 1.查询商品信息
        Product product = productService.getById(productId);
        Long userId = BaseContext.getCurrentId();
        ThrowUtils.throwIf(product == null, ErrorCode.NOT_FOUND_ERROR, "商品不存在!");
        //  利用全局唯一id生成器获得订单id
        long orderId = redisIdWorker.nextId("order");
        // 普通商品可以重复下单 并且库存充足
        ProductOrder productOrder = ProductOrder.builder().userId(userId).productId(productId).id(orderId).build();
        boolean isSuccess = this.save(productOrder);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "下单失败，请稍后重试！");
        return orderId;
    }
}




