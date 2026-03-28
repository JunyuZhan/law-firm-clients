package com.clientservice.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类 - 使用BCrypt加密密码
 */
@Component
public class PasswordUtil {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 密码最小长度（可配置） */
    @Value("${client-service.security.password.min-length:8}")
    private int minLength;

    /** 是否要求包含字母（可配置） */
    @Value("${client-service.security.password.require-letter:true}")
    private boolean requireLetter;

    /** 是否要求包含数字（可配置） */
    @Value("${client-service.security.password.require-digit:true}")
    private boolean requireDigit;

    /** 是否要求包含特殊字符（可配置） */
    @Value("${client-service.security.password.require-special:false}")
    private boolean requireSpecial;

    /**
     * 加密密码
     *
     * @param rawPassword 明文密码
     * @return 加密后的密码
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 验证密码强度（可配置）
     *
     * @param password 密码
     * @return 是否符合要求
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < minLength) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }

        // 根据配置验证密码复杂度
        if (requireLetter && !hasLetter) {
            return false;
        }
        if (requireDigit && !hasDigit) {
            return false;
        }
        if (requireSpecial && !hasSpecial) {
            return false;
        }

        return true;
    }

    /**
     * 获取密码策略描述
     *
     * @return 密码策略描述
     */
    public String getPasswordPolicyDescription() {
        StringBuilder sb = new StringBuilder("密码要求：");
        sb.append("至少").append(minLength).append("位");
        if (requireLetter) {
            sb.append("，包含字母");
        }
        if (requireDigit) {
            sb.append("，包含数字");
        }
        if (requireSpecial) {
            sb.append("，包含特殊字符");
        }
        return sb.toString();
    }
}
