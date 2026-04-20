package com.clientservice.infrastructure.security;

import com.clientservice.application.event.ConfigUpdateEvent;
import com.clientservice.domain.entity.SysConfig;
import com.clientservice.infrastructure.persistence.mapper.SysConfigMapper;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 提供 JWT 密钥，并优先从数据库读取。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecretProvider {

    public static final String JWT_SECRET_CONFIG_KEY = "client-service.jwt.secret";

    private final SysConfigMapper sysConfigMapper;
    private final AtomicReference<String> cachedSecret = new AtomicReference<>();

    @Value("${client-service.jwt.bootstrap-secret:BootstrapJwtSecretKey2026ForArchiveContainerManagerOnly123}")
    private String bootstrapSecret;

    public String getSecret() {
        String secret = cachedSecret.get();
        if (StringUtils.hasText(secret)) {
            return secret;
        }
        return refresh();
    }

    public String refresh() {
        String secret = resolveSecretFromDatabase();
        if (!StringUtils.hasText(secret)) {
            secret = bootstrapSecret;
        }
        cachedSecret.set(secret);
        return secret;
    }

    @EventListener
    public void onConfigUpdated(ConfigUpdateEvent event) {
        if (Objects.equals(JWT_SECRET_CONFIG_KEY, event.getConfigKey())) {
            cachedSecret.set(event.getConfigValue());
            log.info("JWT 密钥缓存已刷新");
        }
    }

    private String resolveSecretFromDatabase() {
        try {
            SysConfig config = sysConfigMapper.selectByConfigKey(JWT_SECRET_CONFIG_KEY);
            if (config != null && StringUtils.hasText(config.getConfigValue())) {
                return config.getConfigValue().trim();
            }
        } catch (Exception ex) {
            log.warn("读取数据库中的 JWT 密钥失败，回退到启动兜底密钥: {}", ex.getMessage());
        }
        return null;
    }
}
