package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.infrastructure.persistence.mapper.ApiKeyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.lenient;

/**
 * ApiKeyService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyService 单元测试")
class ApiKeyServiceTest {

    @Mock
    private ApiKeyMapper apiKeyMapper;

    @Mock
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Mock
    private org.springframework.data.redis.core.ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ApiKeyService apiKeyService;

    private ApiKey validApiKey;

    @BeforeEach
    void setUp() {
        // Mock Redis operations
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        validApiKey = new ApiKey();
        validApiKey.setId(1L);
        validApiKey.setKeyName("测试API密钥");
        validApiKey.setApiKey("test-api-key-12345678901234567890");
        validApiKey.setApiSecret("test-api-secret");
        validApiKey.setLawFirmName("测试律师事务所");
        validApiKey.setEnabled(true);
        validApiKey.setExpiresAt(null); // 永不过期
    }

    @Nested
    @DisplayName("验证API密钥测试")
    class ValidateApiKeyTests {

        @Test
        @DisplayName("有效API密钥应该验证通过")
        void validateApiKey_WithValidKey_ShouldReturnApiKey() {
            // Given
            String apiKey = "test-api-key-12345678901234567890";
            when(apiKeyMapper.selectByApiKey(apiKey)).thenReturn(validApiKey);
            when(apiKeyMapper.updateLastUsedAt(anyLong())).thenReturn(1);

            // When
            ApiKey result = apiKeyService.validateApiKey(apiKey);

            // Then
            assertNotNull(result);
            assertEquals(apiKey, result.getApiKey());
            assertTrue(result.getEnabled());
            verify(apiKeyMapper, times(1)).updateLastUsedAt(validApiKey.getId());
        }

        @Test
        @DisplayName("不存在的API密钥应该抛出异常")
        void validateApiKey_WithNonExistentKey_ShouldThrowException() {
            // Given
            String apiKey = "non-existent-key";
            when(apiKeyMapper.selectByApiKey(apiKey)).thenReturn(null);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                apiKeyService.validateApiKey(apiKey);
            });
        }

        @Test
        @DisplayName("已禁用的API密钥应该抛出异常")
        void validateApiKey_WithDisabledKey_ShouldThrowException() {
            // Given
            String apiKey = "test-api-key-12345678901234567890";
            validApiKey.setEnabled(false);
            when(apiKeyMapper.selectByApiKey(apiKey)).thenReturn(validApiKey);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                apiKeyService.validateApiKey(apiKey);
            });
        }

        @Test
        @DisplayName("已过期的API密钥应该抛出异常")
        void validateApiKey_WithExpiredKey_ShouldThrowException() {
            // Given
            String apiKey = "test-api-key-12345678901234567890";
            validApiKey.setExpiresAt(LocalDateTime.now().minusDays(1));
            when(apiKeyMapper.selectByApiKey(apiKey)).thenReturn(validApiKey);

            // When & Then
            assertThrows(BusinessException.class, () -> {
                apiKeyService.validateApiKey(apiKey);
            });
        }
    }
}
