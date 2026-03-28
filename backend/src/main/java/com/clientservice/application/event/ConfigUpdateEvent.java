package com.clientservice.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 配置更新事件
 * 当系统配置更新时发布此事件，用于通知相关组件刷新缓存
 *
 * @author client-service
 * @since 2026-02-03
 */
@Getter
public class ConfigUpdateEvent extends ApplicationEvent {

    /**
     * 配置键
     */
    private final String configKey;

    /**
     * 配置值
     */
    private final String configValue;

    /**
     * 事件类型：CREATE, UPDATE, DELETE
     */
    private final EventType eventType;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        CREATE,
        UPDATE,
        DELETE
    }

    /**
     * 构造函数
     *
     * @param source 事件源
     * @param configKey 配置键
     * @param configValue 配置值
     * @param eventType 事件类型
     */
    public ConfigUpdateEvent(
            final Object source,
            final String configKey,
            final String configValue,
            final EventType eventType) {
        super(source);
        this.configKey = configKey;
        this.configValue = configValue;
        this.eventType = eventType;
    }
}
