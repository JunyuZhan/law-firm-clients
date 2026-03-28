package com.clientservice.infrastructure.config;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 安全配置验证
 * 
 * <p>在应用启动时验证安全相关配置的强度
 */
@Slf4j
@Component
public class SecurityConfig {

    private final Environment environment;

    @Value("${client-service.jwt.secret}")
    private String jwtSecret;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    /**
     * 应用启动后验证安全配置
     * 注意：测试环境跳过验证，避免测试配置被误判为弱密码
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateSecurityConfig() {
        // 测试环境跳过验证
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) {
                log.info("测试环境跳过 JWT Secret 验证");
                return;
            }
        }
        validateJwtSecret();
    }

    /**
     * 验证 JWT Secret 强度
     * 
     * <p>要求：
     * - 至少32个字符
     * - 包含字母和数字
     * - 建议使用环境变量配置，不要硬编码
     */
    private void validateJwtSecret() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            log.error("❌ JWT Secret 未配置！请设置 client-service.jwt.secret");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "JWT Secret 未配置，请检查配置文件");
        }

        if (jwtSecret.length() < 32) {
            log.error("❌ JWT Secret 长度不足！当前长度: {}，建议至少32个字符", jwtSecret.length());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, 
                String.format("JWT Secret 长度不足（当前%d字符，建议至少32字符），存在安全风险", jwtSecret.length()));
        }

        // 检查是否包含字母和数字
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : jwtSecret.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        if (!hasLetter || !hasDigit) {
            log.warn("⚠️  JWT Secret 强度较弱，建议包含字母和数字");
        }

        // 检查是否是默认值或常见弱密码
        if (isWeakSecret(jwtSecret)) {
            log.error("❌ JWT Secret 使用了弱密码！请使用强密码");
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "JWT Secret 使用了弱密码，存在安全风险");
        }

        log.info("✅ JWT Secret 配置验证通过（长度: {}）", jwtSecret.length());
    }

    /**
     * 检查是否是弱密码
     */
    private boolean isWeakSecret(String secret) {
        String[] weakSecrets = {
            "secret", "password", "123456", "admin", "test",
            "jwt-secret", "my-secret-key", "default-secret"
        };
        
        String lowerSecret = secret.toLowerCase();
        for (String weak : weakSecrets) {
            if (lowerSecret.contains(weak)) {
                return true;
            }
        }
        
        return false;
    }
}
