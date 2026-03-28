package com.clientservice.application.service;

import com.clientservice.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务
 * 
 * <p>管理已失效的 JWT Token，用于登出时使 Token 立即失效
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    /** Token 黑名单的 Redis Key 前缀 */
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";
    /** 用户密码变更时间戳的 Redis Key 前缀 */
    private static final String PASSWORD_CHANGED_KEY_PREFIX = "user:pwd_changed:";

    /**
     * 将 Token 添加到黑名单
     *
     * @param token JWT Token
     */
    public void addToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            // 从 Token 中获取过期时间
            Long expirationTime = jwtUtil.getExpirationTimeFromToken(token);
            if (expirationTime == null) {
                log.warn("无法从 Token 中获取过期时间，使用默认过期时间");
                // 如果无法获取过期时间，使用默认的24小时
                expirationTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
            }

            // 计算剩余有效时间（秒）
            long remainingSeconds = (expirationTime - System.currentTimeMillis()) / 1000;
            if (remainingSeconds > 0) {
                // 使用 Token 的哈希值作为 Key（避免存储完整 Token）
                String tokenHash = getTokenHash(token);
                String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;
                
                // 存储到 Redis，过期时间与 Token 的剩余有效时间一致
                redisTemplate.opsForValue().set(blacklistKey, "1", remainingSeconds, TimeUnit.SECONDS);
                log.info("Token 已添加到黑名单: tokenHash={}, 剩余有效时间={}秒", tokenHash, remainingSeconds);
            } else {
                log.debug("Token 已过期，无需添加到黑名单");
            }
        } catch (Exception e) {
            log.error("添加 Token 到黑名单失败", e);
        }
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String tokenHash = getTokenHash(token);
            String blacklistKey = BLACKLIST_KEY_PREFIX + tokenHash;
            String value = redisTemplate.opsForValue().get(blacklistKey);
            return value != null && "1".equals(value);
        } catch (Exception e) {
            log.error("检查 Token 黑名单失败", e);
            // 出错时返回 false，避免影响正常流程
            return false;
        }
    }

    /**
     * 记录用户密码变更时间，使该时间之前签发的所有 Token 失效
     *
     * @param userId 用户ID
     */
    public void invalidateUserTokens(Long userId) {
        try {
            String key = PASSWORD_CHANGED_KEY_PREFIX + userId;
            // 存储当前时间戳，TTL 与最长 Token 有效期一致（默认48小时，留余量）
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 48, TimeUnit.HOURS);
            log.info("用户所有 Token 已失效: userId={}", userId);
        } catch (Exception e) {
            log.error("记录用户密码变更时间失败: userId={}", userId, e);
        }
    }

    /**
     * 检查 Token 是否因用户密码变更而失效
     *
     * @param userId  用户ID
     * @param issuedAt Token 签发时间（毫秒）
     * @return 是否已失效
     */
    public boolean isTokenInvalidatedByPasswordChange(Long userId, long issuedAt) {
        try {
            String key = PASSWORD_CHANGED_KEY_PREFIX + userId;
            String changedAtStr = redisTemplate.opsForValue().get(key);
            if (changedAtStr == null) {
                return false;
            }
            long changedAt = Long.parseLong(changedAtStr);
            return issuedAt < changedAt;
        } catch (Exception e) {
            log.error("检查用户密码变更状态失败: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 获取 Token 的哈希值（用于 Redis Key）
     * 使用 SHA-256 哈希，避免冲突
     */
    private String getTokenHash(String token) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            log.error("SHA-256 算法不可用，使用简单哈希", e);
            // 降级方案：使用简单哈希
            return String.valueOf(token.hashCode());
        }
    }
}
