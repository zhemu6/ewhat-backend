package com.lushihao.ewhatbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lushihao.ewhatbackend.context.BaseContext;
import com.lushihao.ewhatbackend.context.TenantContextHolder;
import com.lushihao.ewhatbackend.exception.ErrorCode;
import com.lushihao.ewhatbackend.exception.ThrowUtils;
import com.lushihao.ewhatbackend.mapper.ProductOrderMapper;
import com.lushihao.ewhatbackend.model.entity.PointsRecord;
import com.lushihao.ewhatbackend.model.entity.Product;
import com.lushihao.ewhatbackend.model.entity.ProductOrder;
import com.lushihao.ewhatbackend.model.entity.SeckillProduct;
import com.lushihao.ewhatbackend.model.entity.User;
import com.lushihao.ewhatbackend.model.vo.ProductOrderVO;
import com.lushihao.ewhatbackend.service.PointsRecordService;
import com.lushihao.ewhatbackend.service.ProductOrderService;
import com.lushihao.ewhatbackend.service.ProductService;
import com.lushihao.ewhatbackend.service.SeckillProductService;
import com.lushihao.ewhatbackend.service.UserService;
import com.lushihao.ewhatbackend.utils.RedisIdWorker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder>
        implements ProductOrderService {
    private final SeckillProductService seckillProductService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisIdWorker redisIdWorker;
    private final ProductService productService;
    private final UserService userService;
    private final PointsRecordService pointsRecordService;
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
            log.debug("消费者组已存在或创建失败", e);
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
        Long schoolId = TenantContextHolder.getSchoolId();
        ThrowUtils.throwIf(product == null, ErrorCode.NOT_FOUND_ERROR, "商品不存在!");
        //  利用全局唯一id生成器获得订单id
        long orderId = redisIdWorker.nextId("order");
        // 普通商品可以重复下单 并且库存充足
        ProductOrder productOrder = ProductOrder.builder()
                .id(orderId)
                .userId(userId)
                .productId(productId)
                .schoolId(schoolId)
                .orderType(0)
                .status(1)
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .build();
        boolean isSuccess = this.save(productOrder);
        ThrowUtils.throwIf(!isSuccess, ErrorCode.OPERATION_ERROR, "下单失败，请稍后重试！");
        return orderId;
    }

    /**
     * 支付订单（使用积分）
     *
     * @param orderId 订单id
     * @return 支付后的订单
     */
    @Override
    @Transactional
    public ProductOrder payOrder(Long orderId) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查询订单
        ProductOrder order = this.getById(orderId);
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        ThrowUtils.throwIf(!order.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该订单");
        ThrowUtils.throwIf(order.getStatus() != 1, ErrorCode.OPERATION_ERROR, "订单状态异常，无法支付");

        // 2. 检查订单是否过期
        if (order.getExpireTime() != null && order.getExpireTime().isBefore(LocalDateTime.now())) {
            // 自动取消过期订单
            order.setStatus(4);
            this.updateById(order);
            ThrowUtils.throwIf(true, ErrorCode.OPERATION_ERROR, "订单已过期，请重新下单");
        }

        // 3. 查询商品信息
        Product product = productService.getById(order.getProductId());
        ThrowUtils.throwIf(product == null, ErrorCode.NOT_FOUND_ERROR, "商品不存在");

        // 4. 查询用户积分
        User user = userService.getById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(user.getPoints() < product.getPayPoints(), ErrorCode.OPERATION_ERROR, "积分不足，无法支付");

        // 5. 扣除积分
        boolean deductSuccess = userService.update()
                .setSql("points = points - " + product.getPayPoints())
                .eq("id", userId)
                .ge("points", product.getPayPoints())
                .update();
        ThrowUtils.throwIf(!deductSuccess, ErrorCode.OPERATION_ERROR, "积分扣除失败，请重试");

        // 6. 生成兑换码（8位字母数字组合）
        String exchangeCode = generateExchangeCode();

        // 7. 更新订单状态
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        order.setExchangeCode(exchangeCode);
        this.updateById(order);

        // 8. 记录积分流水
        PointsRecord record = PointsRecord.builder()
                .userId(userId)
                .schoolId(TenantContextHolder.getSchoolId())
                .points(-product.getPayPoints())
                .type(2)
                .orderId(orderId)
                .description("购买商品：" + product.getName())
                .build();
        pointsRecordService.save(record);

        return order;
    }

    /**
     * 生成兑换码
     */
    private String generateExchangeCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    /**
     * 核销订单
     *
     * @param exchangeCode 兑换码
     * @return 核销后的订单
     */
    @Override
    @Transactional
    public ProductOrder useOrder(String exchangeCode) {
        // 1. 查询订单
        ProductOrder order = this.query().eq("exchange_code", exchangeCode).one();
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "兑换码无效");
        ThrowUtils.throwIf(order.getStatus() != 2, ErrorCode.OPERATION_ERROR, "订单状态异常，无法核销");

        // 2. 验证管理员权限（检查是否是该校管理员）
        Long adminSchoolId = TenantContextHolder.getSchoolId();
        ThrowUtils.throwIf(!order.getSchoolId().equals(adminSchoolId), ErrorCode.NO_AUTH_ERROR, "无权核销其他学校的订单");

        // 3. 更新订单状态
        order.setStatus(3);
        order.setUseTime(LocalDateTime.now());
        this.updateById(order);

        return order;
    }

    /**
     * 取消订单
     *
     * @param orderId 订单id
     * @return 是否取消成功
     */
    @Override
    @Transactional
    public Boolean cancelOrder(Long orderId) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查询订单
        ProductOrder order = this.getById(orderId);
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        ThrowUtils.throwIf(!order.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权操作该订单");

        // 2. 只能取消未支付的订单
        if (order.getStatus() != 1) {
            return false;
        }

        // 3. 更新订单状态
        order.setStatus(4);
        return this.updateById(order);
    }

    /**
     * 查询用户的订单列表
     *
     * @param status   订单状态（可选）
     * @param current  当前页
     * @param pageSize 每页大小
     * @return 订单分页
     */
    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<ProductOrderVO> queryUserOrders(Integer status, Long current, Long pageSize) {
        Long userId = BaseContext.getCurrentId();

        // 1. 构建查询条件
        Page<ProductOrder> page = new Page<>(current, pageSize);
        var query = this.query().eq("user_id", userId);
        if (status != null) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");

        // 2. 查询订单
        Page<ProductOrder> orderPage = this.page(page, query);

        // 3. 转换为VO
        return orderPage.convert(this::convertToVO);
    }

    /**
     * 查询订单详情
     *
     * @param orderId 订单id
     * @return 订单详情VO
     */
    @Override
    public ProductOrderVO queryOrderDetail(Long orderId) {
        Long userId = BaseContext.getCurrentId();

        // 1. 查询订单
        ProductOrder order = this.getById(orderId);
        ThrowUtils.throwIf(order == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        ThrowUtils.throwIf(!order.getUserId().equals(userId), ErrorCode.NO_AUTH_ERROR, "无权查看该订单");

        // 2. 转换为VO
        return convertToVO(order);
    }

    /**
     * 将订单转换为VO
     */
    private ProductOrderVO convertToVO(ProductOrder order) {
        // 查询商品信息
        Product product = productService.getById(order.getProductId());

        String statusDesc = switch (order.getStatus()) {
            case 1 -> "未支付";
            case 2 -> "已支付";
            case 3 -> "已核销";
            case 4 -> "已取消";
            case 5 -> "退款中";
            case 6 -> "已退款";
            default -> "未知状态";
        };

        return ProductOrderVO.builder()
                .id(order.getId())
                .productId(order.getProductId())
                .productName(product != null ? product.getName() : "")
                .productImage(product != null ? product.getImages() : "")
                .payPoints(product != null ? product.getPayPoints() : 0)
                .status(order.getStatus())
                .statusDesc(statusDesc)
                .exchangeCode(order.getStatus() == 2 ? order.getExchangeCode() : null)
                .exchangeCodeImg(order.getExchangeCodeImg())
                .createTime(order.getCreateTime())
                .expireTime(order.getExpireTime())
                .payTime(order.getPayTime())
                .useTime(order.getUseTime())
                .build();
    }
}




