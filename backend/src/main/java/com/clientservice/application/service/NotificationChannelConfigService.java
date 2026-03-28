package com.clientservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationChannelConfigService {

    private final SysConfigService sysConfigService;

    @Value("${client-service.notification.email.enabled:false}")
    private boolean emailEnabledDefault;

    @Value("${client-service.notification.sms.enabled:false}")
    private boolean smsEnabledDefault;

    @Value("${client-service.notification.wechat.enabled:false}")
    private boolean wechatEnabledDefault;

    public boolean isEmailEnabled() {
        return getBooleanConfig(
                "client-service.notification.email.enabled",
                "notification.email.enabled",
                emailEnabledDefault);
    }

    public boolean isSmsEnabled() {
        return getBooleanConfig(
                "client-service.notification.sms.enabled",
                "notification.sms.enabled",
                smsEnabledDefault);
    }

    public boolean isWechatEnabled() {
        return getBooleanConfig(
                "client-service.notification.wechat.enabled",
                "notification.wechat.enabled",
                wechatEnabledDefault);
    }

    public String getSmsProvider() {
        return getStringConfig(
                "client-service.notification.sms.provider",
                "notification.sms.provider",
                "aliyun");
    }

    public String getWechatTemplateId() {
        return getStringConfig(
                "client-service.notification.wechat.template-id",
                "notification.wechat.template-id",
                "");
    }

    private boolean getBooleanConfig(
            final String primaryKey,
            final String fallbackKey,
            final boolean defaultValue) {
        String value = sysConfigService.getConfigValue(primaryKey);
        if (value == null) {
            value = sysConfigService.getConfigValue(fallbackKey);
        }
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    private String getStringConfig(
            final String primaryKey,
            final String fallbackKey,
            final String defaultValue) {
        String value = sysConfigService.getConfigValue(primaryKey);
        if (value == null) {
            value = sysConfigService.getConfigValue(fallbackKey, defaultValue);
        }
        return value != null ? value : defaultValue;
    }
}
