package com.clientservice.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境缓存配置（使用本地缓存，不依赖Redis）
 */
@Configuration
@EnableCaching
@Profile("test")
public class TestCacheConfig {

    /**
     * 本地缓存管理器（测试环境使用）
     */
    @Bean
    @Primary
    public CacheManager testCacheManager() {
        return new ConcurrentMapCacheManager("apiKey", "sysConfig", "matter", "notificationTemplate");
    }
}
