package com.clientservice.common.util;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 速率限制工具类
 * 
 * <p>提供通用的IP锁定/限制检查逻辑，避免代码重复
 */
@Slf4j
public class RateLimitUtil {

    /**
     * 检查IP是否被锁定/限制
     *
     * @param redisTemplate Redis模板
     * @param lockKey Redis Key（包含IP地址）
     * @param ipAddress IP地址（用于日志记录）
     * @param lockType 锁定类型（用于错误消息，如"登录"、"请求"）
     * @throws BusinessException 如果IP被锁定/限制
     */
    public static void checkIpLockedOrLimited(
            StringRedisTemplate redisTemplate,
            String lockKey,
            String ipAddress,
            String lockType) {
        
        String lockedUntil = redisTemplate.opsForValue().get(lockKey);
        
        if (lockedUntil != null && !lockedUntil.isEmpty()) {
            try {
                long lockedUntilMillis = Long.parseLong(lockedUntil);
                long now = System.currentTimeMillis();
                
                if (now < lockedUntilMillis) {
                    long remainingMinutes = (lockedUntilMillis - now) / (60 * 1000);
                    log.warn("IP{}被锁定: ip={}, 剩余锁定时间={}分钟", lockType, ipAddress, remainingMinutes);
                    throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, 
                        String.format("该IP%s尝试次数过多，已被锁定%d分钟，请稍后再试", lockType, remainingMinutes));
                } else {
                    // 锁定已过期，清除锁定记录
                    redisTemplate.delete(lockKey);
                }
            } catch (NumberFormatException e) {
                // Redis中的值格式错误，清除无效记录
                log.warn("IP{}锁定记录格式错误，清除记录: ip={}, value={}", lockType, ipAddress, lockedUntil);
                redisTemplate.delete(lockKey);
            }
        }
    }

    /**
     * 检查IP是否被限制（用于请求速率限制）
     *
     * @param redisTemplate Redis模板
     * @param limitKey Redis Key（包含IP地址）
     * @param ipAddress IP地址（用于日志记录）
     * @throws BusinessException 如果IP被限制
     */
    public static void checkIpRateLimited(
            StringRedisTemplate redisTemplate,
            String limitKey,
            String ipAddress) {
        
        String limitedUntil = redisTemplate.opsForValue().get(limitKey);
        
        if (limitedUntil != null && !limitedUntil.isEmpty()) {
            try {
                long limitedUntilMillis = Long.parseLong(limitedUntil);
                long now = System.currentTimeMillis();
                
                if (now < limitedUntilMillis) {
                    long remainingMinutes = (limitedUntilMillis - now) / (60 * 1000);
                    log.warn("IP请求被限制: ip={}, 剩余限制时间={}分钟", ipAddress, remainingMinutes);
                    throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS,
                        String.format("请求过于频繁，已被限制%d分钟，请稍后再试", remainingMinutes));
                } else {
                    // 限制已过期，清除限制记录
                    redisTemplate.delete(limitKey);
                }
            } catch (NumberFormatException e) {
                // Redis中的值格式错误，清除无效记录
                log.warn("IP限制记录格式错误，清除记录: ip={}, value={}", ipAddress, limitedUntil);
                redisTemplate.delete(limitKey);
            }
        }
    }
}
