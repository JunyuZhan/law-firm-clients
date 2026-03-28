package com.clientservice.infrastructure.config;

import com.clientservice.application.event.ConfigUpdateEvent;
import com.clientservice.infrastructure.notification.aliyun.AliyunSmsClient;
import com.clientservice.infrastructure.notification.tencent.TencentSmsClient;
import com.clientservice.infrastructure.notification.wechat.WechatAccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 配置更新事件监听器
 * 监听配置更新事件，自动清除相关客户端缓存，确保配置立即生效
 *
 * @author client-service
 * @since 2026-02-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigUpdateListener {

    private final AliyunSmsClient aliyunSmsClient;
    private final TencentSmsClient tencentSmsClient;
    private final WechatAccessTokenService wechatAccessTokenService;

    /**
     * 监听配置更新事件
     * 根据配置键自动清除相关客户端缓存
     *
     * @param event 配置更新事件
     */
    @Async
    @EventListener
    public void handleConfigUpdate(final ConfigUpdateEvent event) {
        String configKey = event.getConfigKey();
        ConfigUpdateEvent.EventType eventType = event.getEventType();

        log.info("收到配置更新事件: configKey={}, eventType={}", configKey, eventType);

        // 根据配置键判断需要清除哪些客户端缓存
        if (configKey != null) {
            // 阿里云短信配置更新
            if (configKey.startsWith("client-service.notification.sms.aliyun.")
                    || configKey.startsWith("notification.sms.aliyun.")) {
                try {
                    aliyunSmsClient.clearClientCache();
                    log.info("已清除阿里云短信客户端缓存: configKey={}", configKey);
                } catch (Exception e) {
                    log.error("清除阿里云短信客户端缓存失败: configKey={}", configKey, e);
                }
            }

            // 腾讯云短信配置更新
            if (configKey.startsWith("client-service.notification.sms.tencent.")
                    || configKey.startsWith("notification.sms.tencent.")) {
                try {
                    tencentSmsClient.clearClientCache();
                    log.info("已清除腾讯云短信客户端缓存: configKey={}", configKey);
                } catch (Exception e) {
                    log.error("清除腾讯云短信客户端缓存失败: configKey={}", configKey, e);
                }
            }

            // 微信配置更新（包括 app-id 和 app-secret）
            if (configKey.startsWith("client-service.notification.wechat.")
                    || configKey.startsWith("notification.wechat.")) {
                try {
                    wechatAccessTokenService.clearAccessTokenCache();
                    log.info("已清除微信AccessToken缓存: configKey={}", configKey);
                } catch (Exception e) {
                    log.error("清除微信AccessToken缓存失败: configKey={}", configKey, e);
                }
            }

            // 如果更新的是短信提供商配置，需要清除两个客户端的缓存
            if (configKey.equals("client-service.notification.sms.provider")
                    || configKey.equals("notification.sms.provider")) {
                try {
                    aliyunSmsClient.clearClientCache();
                    tencentSmsClient.clearClientCache();
                    log.info("已清除所有短信客户端缓存: configKey={}", configKey);
                } catch (Exception e) {
                    log.error("清除短信客户端缓存失败: configKey={}", configKey, e);
                }
            }
        }
    }
}
