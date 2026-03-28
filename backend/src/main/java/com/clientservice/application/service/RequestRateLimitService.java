package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.util.RateLimitUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 请求速率限制服务
 * 
 * <p>防止DDoS攻击，基于IP地址限制请求频率
 * 
 * <h3>算法说明：</h3>
 * <ul>
 *   <li><b>固定窗口计数算法</b>：使用两个固定时间窗口（1分钟和1小时）统计请求次数</li>
 *   <li><b>双重限制</b>：同时检查短期（1分钟）和长期（1小时）的请求频率</li>
 *   <li><b>阈值触发限制</b>：当任一窗口的请求次数超过阈值时，限制IP地址</li>
 *   <li><b>限制时间</b>：限制后持续一段时间（默认10分钟），期间禁止该IP的所有请求</li>
 *   <li><b>自动过期</b>：使用Redis的过期机制自动清理过期的计数记录和限制记录</li>
 * </ul>
 * 
 * <h3>工作流程：</h3>
 * <ol>
 *   <li>每次请求前调用 {@link #checkRateLimit(String, String)} 检查速率限制</li>
 *   <li>使用 Redis INCR 原子性地增加请求计数</li>
 *   <li>检查是否超过阈值，如果超过则限制IP并抛出异常</li>
 * </ol>
 * 
 * <h3>Redis数据结构：</h3>
 * <ul>
 *   <li><b>分钟计数</b>：Key = "rate:limit:ip:{ipAddress}:minute", Value = 请求次数, TTL = 1分钟</li>
 *   <li><b>小时计数</b>：Key = "rate:limit:ip:{ipAddress}:hour", Value = 请求次数, TTL = 1小时</li>
 *   <li><b>限制记录</b>：Key = "rate:limited:ip:{ipAddress}", Value = 限制到期时间戳(毫秒), TTL = 限制时间</li>
 * </ul>
 * 
 * <h3>配置参数：</h3>
 * <ul>
 *   <li>{@code max-requests-per-minute}：每分钟最大请求数（默认60）</li>
 *   <li>{@code max-requests-per-hour}：每小时最大请求数（默认1000）</li>
 *   <li>{@code rate-limit-duration-minutes}：限制持续时间（默认10分钟）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestRateLimitService {

    private final StringRedisTemplate redisTemplate;

    /** IP请求计数的Redis Key前缀 */
    private static final String IP_REQUEST_COUNT_KEY_PREFIX = "rate:limit:ip:";

    /** IP被限制的Redis Key前缀 */
    private static final String IP_RATE_LIMITED_KEY_PREFIX = "rate:limited:ip:";

    /** 每个IP允许的最大请求次数（1分钟内，可配置） */
    @Value("${client-service.security.request-rate-limit.max-requests-per-minute:60}")
    private int maxRequestsPerMinute;

    /** 每个IP允许的最大请求次数（1小时内，可配置） */
    @Value("${client-service.security.request-rate-limit.max-requests-per-hour:1000}")
    private int maxRequestsPerHour;

    /** IP被限制后的限制时间（分钟，可配置） */
    @Value("${client-service.security.request-rate-limit.rate-limit-duration-minutes:10}")
    private int rateLimitDurationMinutes;

    /**
     * 检查IP请求频率（防止DDoS）
     * 
     * <p><b>算法步骤：</b>
     * <ol>
     *   <li>首先检查IP是否已被限制（如果已限制则直接抛出异常）</li>
     *   <li>使用 Redis INCR 原子性地增加1分钟窗口的请求计数</li>
     *   <li>如果是第一次请求（count == 1），设置键的过期时间为1分钟</li>
     *   <li>检查1分钟窗口是否超过阈值，如果超过则限制IP</li>
     *   <li>同样处理1小时窗口的请求计数</li>
     * </ol>
     * 
     * <p><b>双重限制机制：</b>
     * <ul>
     *   <li>短期限制（1分钟）：防止突发流量攻击</li>
     *   <li>长期限制（1小时）：防止持续高频请求</li>
     * </ul>
     * 
     * <p><b>注意：</b>
     * <ul>
     *   <li>使用 Redis INCR 确保并发安全，多个请求同时计数时不会丢失</li>
     *   <li>使用 Redis 过期机制自动清理过期的计数，无需手动清理</li>
     *   <li>限制IP后，后续请求会在第一步被拦截，不会继续计数</li>
     * </ul>
     *
     * @param ipAddress IP地址
     * @param endpoint 请求端点（可选，用于更细粒度的控制，当前未使用）
     * @throws BusinessException 如果IP请求频率过高
     */
    public void checkRateLimit(String ipAddress, String endpoint) {
        // 第一步：检查IP是否已被限制（如果已限制则直接抛出异常）
        checkIpRateLimited(ipAddress);

        // 第二步：检查1分钟内的请求次数（短期限制，防止突发流量）
        String minuteKey = IP_REQUEST_COUNT_KEY_PREFIX + ipAddress + ":minute";
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);
        // 如果是第一次请求，设置过期时间为1分钟（固定窗口）
        if (minuteCount == 1) {
            redisTemplate.expire(minuteKey, 1, TimeUnit.MINUTES);
        }

        // 如果1分钟内请求次数超过阈值，限制IP
        if (minuteCount != null && minuteCount > maxRequestsPerMinute) {
            log.warn("IP请求频率过高（1分钟）: ip={}, count={}", ipAddress, minuteCount);
            rateLimitIp(ipAddress);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }

        // 第三步：检查1小时内的请求次数（长期限制，防止持续高频请求）
        String hourKey = IP_REQUEST_COUNT_KEY_PREFIX + ipAddress + ":hour";
        Long hourCount = redisTemplate.opsForValue().increment(hourKey);
        // 如果是第一次请求，设置过期时间为1小时（固定窗口）
        if (hourCount == 1) {
            redisTemplate.expire(hourKey, 1, TimeUnit.HOURS);
        }

        // 如果1小时内请求次数超过阈值，限制IP
        if (hourCount != null && hourCount > maxRequestsPerHour) {
            log.warn("IP请求频率过高（1小时）: ip={}, count={}", ipAddress, hourCount);
            rateLimitIp(ipAddress);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁，请稍后再试");
        }
    }

    /**
     * 检查IP是否已被限制
     */
    private void checkIpRateLimited(String ipAddress) {
        String limitKey = IP_RATE_LIMITED_KEY_PREFIX + ipAddress;
        RateLimitUtil.checkIpRateLimited(redisTemplate, limitKey, ipAddress);
    }

    /**
     * 限制IP地址
     */
    private void rateLimitIp(String ipAddress) {
        String limitKey = IP_RATE_LIMITED_KEY_PREFIX + ipAddress;
        long limitedUntil = System.currentTimeMillis() + (rateLimitDurationMinutes * 60 * 1000L);

        redisTemplate.opsForValue().set(limitKey, String.valueOf(limitedUntil),
            rateLimitDurationMinutes, TimeUnit.MINUTES);

        log.warn("IP请求被限制: ip={}, 限制时间={}分钟", ipAddress, rateLimitDurationMinutes);
    }
}
