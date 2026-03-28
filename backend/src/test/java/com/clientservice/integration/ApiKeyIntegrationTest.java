package com.clientservice.integration;

import com.clientservice.application.service.ApiKeyService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ApiKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API密钥集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("API密钥集成测试")
class ApiKeyIntegrationTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Test
    @DisplayName("验证有效的API密钥应该成功")
    void validateApiKey_WithValidKey_ShouldSuccess() {
        // Given - 使用测试数据中的API密钥
        String apiKey = "test-api-key-12345678901234567890";

        // When
        ApiKey result = apiKeyService.validateApiKey(apiKey);

        // Then
        assertNotNull(result);
        assertEquals(apiKey, result.getApiKey());
        assertTrue(result.getEnabled());
    }

    @Test
    @DisplayName("验证不存在的API密钥应该抛出异常")
    void validateApiKey_WithNonExistentKey_ShouldThrowException() {
        // When & Then
        assertThrows(BusinessException.class, () -> {
            apiKeyService.validateApiKey("non-existent-key");
        });
    }

    @Test
    @DisplayName("Bearer Token格式应该正确处理")
    void validateApiKey_WithBearerToken_ShouldSuccess() {
        // Given
        String bearerToken = "Bearer test-api-key-12345678901234567890";

        // When
        ApiKey result = apiKeyService.validateApiKey(bearerToken);

        // Then
        assertNotNull(result);
        assertEquals("test-api-key-12345678901234567890", result.getApiKey());
    }
}
