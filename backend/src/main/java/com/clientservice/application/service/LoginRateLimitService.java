package com.clientservice.application.service;

import com.clientservice.common.util.RateLimitUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录速率限制服务
 * 
 * <p>防止撞库攻击，基于IP地址限制登录尝试次数
 * 
 * <h3>算法说明：</h3>
 * <ul>
 *   <li><b>固定窗口计数算法</b>：使用固定时间窗口（默认15分钟）统计登录失败次数</li>
 *   <li><b>阈值触发锁定</b>：当失败次数达到阈值（默认10次）时，锁定IP地址</li>
 *   <li><b>锁定时间</b>：锁定后持续一段时间（默认30分钟），期间禁止该IP的所有登录尝试</li>
 *   <li><b>自动过期</b>：使用Redis的过期机制自动清理过期的尝试记录和锁定记录</li>
 * </ul>
 * 
 * <h3>工作流程：</h3>
 * <ol>
 *   <li>登录失败时调用 {@link #recordFailedAttempt(String)} 记录失败次数</li>
 *   <li>每次登录前调用 {@link #checkIpLocked(String)} 检查IP是否被锁定</li>
 *   <li>登录成功时调用 {@link #clearAttempts(String)} 清除失败记录</li>
 * </ol>
 * 
 * <h3>Redis数据结构：</h3>
 * <ul>
 *   <li><b>尝试记录</b>：Key = "login:attempt:ip:{ipAddress}", Value = 失败次数, TTL = 时间窗口</li>
 *   <li><b>锁定记录</b>：Key = "login:locked:ip:{ipAddress}", Value = 锁定到期时间戳(毫秒), TTL = 锁定时间</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginRateLimitService {

    private final StringRedisTemplate redisTemplate;

    /** IP登录尝试次数的Redis Key前缀 */
    private static final String IP_LOGIN_ATTEMPT_KEY_PREFIX = "login:attempt:ip:";

    /** IP被锁定的Redis Key前缀 */
    private static final String IP_LOCKED_KEY_PREFIX = "login:locked:ip:";

    /** 每个IP允许的最大登录尝试次数（可配置） */
    @Value("${client-service.security.login-rate-limit.max-attempts-per-ip:10}")
    private int maxAttemptsPerIp;

    /** IP登录尝试的时间窗口（分钟，可配置） */
    @Value("${client-service.security.login-rate-limit.attempt-window-minutes:15}")
    private int attemptWindowMinutes;

    /** IP被锁定后的锁定时间（分钟，可配置） */
    @Value("${client-service.security.login-rate-limit.ip-lock-duration-minutes:30}")
    private int ipLockDurationMinutes;

    /**
     * 检查IP是否被锁定
     *
     * @param ipAddress IP地址
     * @throws BusinessException 如果IP被锁定
     */
    public void checkIpLocked(String ipAddress) {
        String lockKey = IP_LOCKED_KEY_PREFIX + ipAddress;
        RateLimitUtil.checkIpLockedOrLimited(redisTemplate, lockKey, ipAddress, "登录");
    }

    /**
     * 记录登录尝试（失败）
     * 
     * <p><b>算法步骤：</b>
     * <ol>
     *   <li>使用 Redis INCR 命令原子性地增加失败次数</li>
     *   <li>如果是第一次失败（attempts == 1），设置键的过期时间为时间窗口</li>
     *   <li>检查是否达到阈值，如果达到则锁定IP并清除尝试记录</li>
     * </ol>
     * 
     * <p><b>注意：</b>使用 Redis INCR 确保并发安全，多个请求同时记录失败时不会丢失计数
     *
     * @param ipAddress IP地址
     */
    public void recordFailedAttempt(String ipAddress) {
        String attemptKey = IP_LOGIN_ATTEMPT_KEY_PREFIX + ipAddress;
        
        // 使用 Redis INCR 原子性地增加尝试次数（并发安全）
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        
        // 如果是第一次尝试，设置过期时间（固定窗口）
        // 过期后自动清除记录，重新开始计数
        if (attempts == 1) {
            redisTemplate.expire(attemptKey, attemptWindowMinutes, TimeUnit.MINUTES);
        }
        
        log.debug("IP登录失败尝试记录: ip={}, 当前尝试次数={}", ipAddress, attempts);
        
        // 如果超过最大尝试次数，锁定IP
        if (attempts != null && attempts >= maxAttemptsPerIp) {
            lockIp(ipAddress);
            // 清除尝试次数记录（因为已经锁定，不再需要计数）
            redisTemplate.delete(attemptKey);
        }
    }

    /**
     * 清除登录尝试记录（登录成功时调用）
     *
     * @param ipAddress IP地址
     */
    public void clearAttempts(String ipAddress) {
        String attemptKey = IP_LOGIN_ATTEMPT_KEY_PREFIX + ipAddress;
        redisTemplate.delete(attemptKey);
        log.debug("清除IP登录尝试记录: ip={}", ipAddress);
    }

    /**
     * 锁定IP地址
     *
     * @param ipAddress IP地址
     */
    private void lockIp(String ipAddress) {
        String lockKey = IP_LOCKED_KEY_PREFIX + ipAddress;
        long lockedUntil = System.currentTimeMillis() + (ipLockDurationMinutes * 60 * 1000L);
        
        redisTemplate.opsForValue().set(lockKey, String.valueOf(lockedUntil), 
            ipLockDurationMinutes, TimeUnit.MINUTES);
        
        log.warn("IP登录被锁定: ip={}, 锁定时间={}分钟", ipAddress, ipLockDurationMinutes);
    }

    /**
     * 获取IP的剩余尝试次数
     *
     * @param ipAddress IP地址
     * @return 剩余尝试次数（如果未记录则返回maxAttemptsPerIp）
     */
    public int getRemainingAttempts(String ipAddress) {
        String attemptKey = IP_LOGIN_ATTEMPT_KEY_PREFIX + ipAddress;
        String attemptsStr = redisTemplate.opsForValue().get(attemptKey);
        
        if (attemptsStr == null || attemptsStr.isEmpty()) {
            return maxAttemptsPerIp;
        }
        
        try {
            int attempts = Integer.parseInt(attemptsStr);
            return Math.max(0, maxAttemptsPerIp - attempts);
        } catch (NumberFormatException e) {
            // Redis中的值格式错误，清除无效记录并返回最大尝试次数
            log.warn("IP尝试次数记录格式错误，清除记录: ip={}, value={}", ipAddress, attemptsStr);
            redisTemplate.delete(attemptKey);
            return maxAttemptsPerIp;
        }
    }
}
