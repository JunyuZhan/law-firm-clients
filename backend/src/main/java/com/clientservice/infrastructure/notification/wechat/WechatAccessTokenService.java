package com.clientservice.infrastructure.notification.wechat;

import com.clientservice.application.service.SysConfigService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 微信AccessToken管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAccessTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final WechatApiClient wechatApiClient;
    private final SysConfigService sysConfigService;

    @Value("${client-service.notification.wechat.app-id:}")
    private String appIdDefault;

    @Value("${client-service.notification.wechat.app-secret:}")
    private String appSecretDefault;

    /**
     * 获取 App ID（优先从数据库读取）
     */
    private String getAppId() {
        return sysConfigService.getConfigValue("client-service.notification.wechat.app-id", appIdDefault);
    }

    /**
     * 获取 App Secret（优先从数据库读取）
     */
    private String getAppSecret() {
        return sysConfigService.getConfigValue("client-service.notification.wechat.app-secret", appSecretDefault);
    }

    private static final String ACCESS_TOKEN_KEY_PREFIX = "wechat:access_token:";
    private static final int CACHE_EXPIRE_SECONDS = 7000; // 缓存提前10分钟过期（微信AccessToken有效期2小时，提前10分钟过期）

    /**
     * 获取AccessToken（带缓存）
     *
     * @return AccessToken
     */
    public String getAccessToken() {
        if (!isConfigured()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "微信AppID或AppSecret未配置");
        }

        String appId = getAppId();
        String cacheKey = ACCESS_TOKEN_KEY_PREFIX + appId;
        
        // 先从缓存获取
        String cachedToken = redisTemplate.opsForValue().get(cacheKey);
        if (cachedToken != null && !cachedToken.isEmpty()) {
            log.debug("从缓存获取微信AccessToken: appId={}", appId);
            return cachedToken;
        }

        // 缓存未命中，从微信API获取（需要同步，避免并发请求）
        synchronized (this) {
            // 双重检查，避免重复获取
            cachedToken = redisTemplate.opsForValue().get(cacheKey);
            if (cachedToken != null && !cachedToken.isEmpty()) {
                log.debug("从缓存获取微信AccessToken（双重检查）: appId={}", appId);
                return cachedToken;
            }

            try {
                String token = fetchAccessTokenFromWechat();
                
                // 存入缓存
                redisTemplate.opsForValue().set(cacheKey, token, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
                
                log.info("获取微信AccessToken成功: appId={}", appId);
                return token;
            } catch (Exception e) {
                log.error("获取微信AccessToken失败: appId={}", appId, e);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "获取微信AccessToken失败: " + e.getMessage());
            }
        }
    }

    /**
     * 从微信API获取AccessToken
     *
     * @return AccessToken
     */
    private String fetchAccessTokenFromWechat() {
        try {
            String appId = getAppId();
            String appSecret = getAppSecret();
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    appId, appSecret);
            
            String response = wechatApiClient.get(url);
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.has("access_token")) {
                return jsonNode.get("access_token").asText();
            } else {
                String errorMsg = jsonNode.has("errmsg") 
                        ? jsonNode.get("errmsg").asText() 
                        : "未知错误";
                int errorCode = jsonNode.has("errcode") 
                        ? jsonNode.get("errcode").asInt() 
                        : -1;
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                        String.format("获取微信AccessToken失败: errcode=%d, errmsg=%s", errorCode, errorMsg));
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信API获取AccessToken异常", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "调用微信API获取AccessToken异常: " + e.getMessage());
        }
    }

    /**
     * 清除AccessToken缓存（用于强制刷新）
     */
    public void clearAccessTokenCache() {
        String appId = getAppId();
        String cacheKey = ACCESS_TOKEN_KEY_PREFIX + appId;
        redisTemplate.delete(cacheKey);
        log.info("清除微信AccessToken缓存: appId={}", appId);
    }

    /**
     * 检查是否已配置
     *
     * @return 是否已配置
     */
    public boolean isConfigured() {
        String appId = getAppId();
        String appSecret = getAppSecret();
        return appId != null && !appId.isEmpty()
                && appSecret != null && !appSecret.isEmpty();
    }
}
