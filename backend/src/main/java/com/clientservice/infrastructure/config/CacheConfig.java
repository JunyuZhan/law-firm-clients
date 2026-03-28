package com.clientservice.infrastructure.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 缓存配置（生产环境）
 */
@Configuration
@EnableCaching
@Profile("!test")
public class CacheConfig {

    /**
     * Redis缓存管理器（主缓存）
     */
    @Bean
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("apiKey", config.entryTtl(Duration.ofHours(1))) // API密钥缓存1小时
                .withCacheConfiguration("sysConfig", config.entryTtl(Duration.ofHours(2))) // 系统配置缓存2小时
                .withCacheConfiguration("matter", config.entryTtl(Duration.ofMinutes(10))) // 项目数据缓存10分钟
                .withCacheConfiguration("notificationTemplate", config.entryTtl(Duration.ofHours(1))) // 通知模板缓存1小时
                .transactionAware() // 支持事务
                .build();
    }

    /**
     * 本地缓存管理器（备用，当Redis不可用时）
     */
    @Bean
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager("apiKey", "sysConfig", "matter", "notificationTemplate");
    }
}
