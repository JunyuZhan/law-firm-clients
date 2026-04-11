package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.filter.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理后台鉴权服务。
 *
 * <p>当前先收口高风险后台能力到“超级管理员”级别，避免普通后台账号直接接触
 * API 密钥、系统配置、备份与其他运维级操作。
 */
@Service
public class AdminAuthorizationService {

    private final Set<String> superAdminUsernames;

    public AdminAuthorizationService(
            @Value("${client-service.security.super-admin-usernames:admin}") String superAdminUsernames) {
        this.superAdminUsernames = Arrays.stream(superAdminUsernames.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(username -> username.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    public AdminUser requireCurrentUser() {
        AdminUser currentUser = JwtAuthenticationFilter.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        return currentUser;
    }

    public boolean isSuperAdmin(AdminUser user) {
        return user != null
                && StringUtils.hasText(user.getUsername())
                && superAdminUsernames.contains(user.getUsername().trim().toLowerCase(Locale.ROOT));
    }

    public void requireSuperAdmin() {
        AdminUser currentUser = requireCurrentUser();
        if (!isSuperAdmin(currentUser)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅超级管理员可执行该操作");
        }
    }
}
