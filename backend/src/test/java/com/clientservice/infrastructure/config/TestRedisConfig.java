package com.clientservice.infrastructure.config;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * 测试环境Redis配置（Mock实现，不连接真实Redis）
 */
@Configuration
@Profile("test")
public class TestRedisConfig {

    /**
     * Mock StringRedisTemplate（测试环境使用，不连接真实Redis）
     */
    @Bean
    @Primary
    public StringRedisTemplate testStringRedisTemplate() {
        // Mock RedisConnectionFactory
        RedisConnectionFactory connectionFactory = Mockito.mock(RedisConnectionFactory.class);
        RedisConnection connection = Mockito.mock(RedisConnection.class);
        Mockito.lenient().when(connectionFactory.getConnection()).thenReturn(connection);

        // Mock StringRedisTemplate
        StringRedisTemplate template = Mockito.mock(StringRedisTemplate.class);
        Mockito.lenient().when(template.getConnectionFactory()).thenReturn(connectionFactory);
        
        // Mock opsForValue
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);
        Mockito.lenient().when(template.opsForValue()).thenReturn(valueOps);
        
        // 配置默认行为（避免NPE）
        Mockito.lenient().when(valueOps.increment(Mockito.anyString())).thenReturn(1L);
        Mockito.lenient().when(valueOps.increment(Mockito.anyString(), Mockito.anyLong())).thenReturn(1L);
        Mockito.lenient().when(valueOps.get(Mockito.anyString())).thenReturn(null);
        Mockito.lenient().when(template.delete(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(template.hasKey(Mockito.anyString())).thenReturn(false);
        
        return template;
    }
}
