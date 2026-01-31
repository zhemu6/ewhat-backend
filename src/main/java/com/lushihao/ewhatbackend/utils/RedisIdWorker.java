package com.lushihao.ewhatbackend.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 基于Redis的全局唯一ID生成器
 * 分布式系统下用来生成全局唯一ID
 *
 * @author: lushihao
 * @version: 1.0
 * create:   2025-08-13   13:45
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisIdWorker {
    // 开始时间戳 从2022.1.1的0点起始
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    // 时间戳的位数
    private static final int COUNT_BITS = 32;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 获取下一个id
     *
     * @param keyPrefix redis业务前缀
     * @return 返回一个Long格式的id
     */
    public long nextId(String keyPrefix) {
        // 要回事呢工程的ID包含两部分 第一部分是时间戳 第二部分是序列号
        // 1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        // 获取当前时间
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        // 当前时间减去开始时间获得时间戳
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;

        // 2.生成序列号 为每天下得订单设置日期后缀 icr:order:2023:10:10 这是key
        // 2.1 获取当前日期，精确到天 date
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        // 拼接时间戳和序列号并返回 十进制的
        return timeStamp << COUNT_BITS | count;
    }

}
