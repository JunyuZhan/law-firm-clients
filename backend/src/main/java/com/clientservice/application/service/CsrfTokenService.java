package com.clientservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * CSRF Token 服务
 * 
 * <p>生成和验证 CSRF Token，防止跨站请求伪造攻击
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsrfTokenService {

    private final StringRedisTemplate redisTemplate;

    /** CSRF Token 的 Redis Key 前缀 */
    private static final String CSRF_TOKEN_KEY_PREFIX = "csrf:token:";

    /** CSRF Token 有效期（小时，可配置） */
    @Value("${client-service.security.csrf-token.expire-hours:24}")
    private int csrfTokenExpireHours;

    /** 安全随机数生成器 */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成 CSRF Token
     *
     * @param sessionId 会话ID（可以使用用户ID或其他唯一标识）
     * @return CSRF Token
     */
    public String generateToken(String sessionId) {
        // 生成32字节的随机Token
        byte[] tokenBytes = new byte[32];
        RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // 存储到 Redis
        String tokenKey = CSRF_TOKEN_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(tokenKey, token, csrfTokenExpireHours, TimeUnit.HOURS);

        log.debug("生成 CSRF Token: sessionId={}", sessionId);
        return token;
    }

    /**
     * 验证 CSRF Token
     *
     * @param sessionId 会话ID
     * @param token CSRF Token
     * @return 是否验证通过
     */
    public boolean validateToken(String sessionId, String token) {
        if (sessionId == null || sessionId.isEmpty() || token == null || token.isEmpty()) {
            return false;
        }

        String tokenKey = CSRF_TOKEN_KEY_PREFIX + sessionId;
        String storedToken = redisTemplate.opsForValue().get(tokenKey);

        if (storedToken == null) {
            log.debug("CSRF Token 不存在或已过期: sessionId={}", sessionId);
            return false;
        }

        boolean isValid = storedToken.equals(token);
        if (!isValid) {
            log.warn("CSRF Token 验证失败: sessionId={}", sessionId);
        }

        return isValid;
    }

    /**
     * 刷新 CSRF Token（延长有效期）
     *
     * @param sessionId 会话ID
     * @param token CSRF Token
     * @return 是否刷新成功
     */
    public boolean refreshToken(String sessionId, String token) {
        if (!validateToken(sessionId, token)) {
            return false;
        }

        String tokenKey = CSRF_TOKEN_KEY_PREFIX + sessionId;
        redisTemplate.expire(tokenKey, csrfTokenExpireHours, TimeUnit.HOURS);
        log.debug("刷新 CSRF Token: sessionId={}", sessionId);
        return true;
    }

    /**
     * 删除 CSRF Token
     *
     * @param sessionId 会话ID
     */
    public void deleteToken(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        String tokenKey = CSRF_TOKEN_KEY_PREFIX + sessionId;
        redisTemplate.delete(tokenKey);
        log.debug("删除 CSRF Token: sessionId={}", sessionId);
    }
}
