package com.clientservice.application.service;

import com.clientservice.domain.entity.SysConfig;
import com.clientservice.infrastructure.persistence.mapper.SysConfigMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SysConfigService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysConfigService 单元测试")
class SysConfigServiceTest {

    @Mock
    private SysConfigMapper configMapper;

    @InjectMocks
    private SysConfigService sysConfigService;

    private SysConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = SysConfig.builder()
                .id(1L)
                .configKey("test.config.key")
                .configValue("test-value")
                .build();
    }

    @Nested
    @DisplayName("获取配置值测试")
    class GetConfigValueTests {

        @Test
        @DisplayName("获取配置值应该成功")
        void getConfigValue_ShouldSuccess() {
            // Given
            String configKey = "test.config.key";
            when(configMapper.selectByConfigKey(configKey)).thenReturn(testConfig);

            // When
            String result = sysConfigService.getConfigValue(configKey);

            // Then
            assertNotNull(result);
            assertEquals("test-value", result);
            verify(configMapper, times(1)).selectByConfigKey(configKey);
        }

        @Test
        @DisplayName("配置不存在应该返回null")
        void getConfigValue_WhenNotExists_ShouldReturnNull() {
            // Given
            String configKey = "non.existent.key";
            when(configMapper.selectByConfigKey(configKey)).thenReturn(null);

            // When
            String result = sysConfigService.getConfigValue(configKey);

            // Then
            assertNull(result);
            verify(configMapper, times(1)).selectByConfigKey(configKey);
        }

        @Test
        @DisplayName("获取配置值（带默认值）应该成功")
        void getConfigValue_WithDefaultValue_ShouldSuccess() {
            // Given
            String configKey = "test.config.key";
            String defaultValue = "default-value";
            when(configMapper.selectByConfigKey(configKey)).thenReturn(testConfig);

            // When
            String result = sysConfigService.getConfigValue(configKey, defaultValue);

            // Then
            assertNotNull(result);
            assertEquals("test-value", result);
        }

        @Test
        @DisplayName("配置不存在时应该返回默认值")
        void getConfigValue_WithDefaultValue_WhenNotExists_ShouldReturnDefault() {
            // Given
            String configKey = "non.existent.key";
            String defaultValue = "default-value";
            when(configMapper.selectByConfigKey(configKey)).thenReturn(null);

            // When
            String result = sysConfigService.getConfigValue(configKey, defaultValue);

            // Then
            assertNotNull(result);
            assertEquals(defaultValue, result);
        }
    }

    @Nested
    @DisplayName("获取布尔配置值测试")
    class GetBooleanConfigTests {

        @Test
        @DisplayName("获取布尔配置值应该成功")
        void getBooleanConfig_ShouldSuccess() {
            // Given
            String configKey = "test.boolean.key";
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue("true")
                    .build();
            when(configMapper.selectByConfigKey(configKey)).thenReturn(config);

            // When
            boolean result = sysConfigService.getBooleanConfig(configKey, false);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("布尔配置值为false应该返回false")
        void getBooleanConfig_WithFalseValue_ShouldReturnFalse() {
            // Given
            String configKey = "test.boolean.key";
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue("false")
                    .build();
            when(configMapper.selectByConfigKey(configKey)).thenReturn(config);

            // When
            boolean result = sysConfigService.getBooleanConfig(configKey, true);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("配置不存在时应该返回默认值")
        void getBooleanConfig_WhenNotExists_ShouldReturnDefault() {
            // Given
            String configKey = "non.existent.key";
            when(configMapper.selectByConfigKey(configKey)).thenReturn(null);

            // When
            boolean result = sysConfigService.getBooleanConfig(configKey, true);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("无效的布尔值应该返回默认值")
        void getBooleanConfig_WithInvalidValue_ShouldReturnDefault() {
            // Given
            String configKey = "test.boolean.key";
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue("invalid")
                    .build();
            when(configMapper.selectByConfigKey(configKey)).thenReturn(config);

            // When
            boolean result = sysConfigService.getBooleanConfig(configKey, true);

            // Then
            assertFalse(result); // Boolean.parseBoolean("invalid") returns false
        }
    }

    @Nested
    @DisplayName("获取整数配置值测试")
    class GetIntConfigTests {

        @Test
        @DisplayName("获取整数配置值应该成功")
        void getIntConfig_ShouldSuccess() {
            // Given
            String configKey = "test.int.key";
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue("123")
                    .build();
            when(configMapper.selectByConfigKey(configKey)).thenReturn(config);

            // When
            int result = sysConfigService.getIntConfig(configKey, 0);

            // Then
            assertEquals(123, result);
        }

        @Test
        @DisplayName("配置不存在时应该返回默认值")
        void getIntConfig_WhenNotExists_ShouldReturnDefault() {
            // Given
            String configKey = "non.existent.key";
            when(configMapper.selectByConfigKey(configKey)).thenReturn(null);

            // When
            int result = sysConfigService.getIntConfig(configKey, 999);

            // Then
            assertEquals(999, result);
        }

        @Test
        @DisplayName("无效的整数值应该返回默认值")
        void getIntConfig_WithInvalidValue_ShouldReturnDefault() {
            // Given
            String configKey = "test.int.key";
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue("not-a-number")
                    .build();
            when(configMapper.selectByConfigKey(configKey)).thenReturn(config);

            // When
            int result = sysConfigService.getIntConfig(configKey, 999);

            // Then
            assertEquals(999, result);
        }

        @Test
        @DisplayName("负数应该正确处理")
        void getIntConfig_WithNegativeValue_ShouldSuccess() {
            // Given
            String configKey = "test.int.key";
            SysConfig config = SysConfig.builder()
                    .configKey(configKey)
                    .configValue("-456")
                    .build();
            when(configMapper.selectByConfigKey(configKey)).thenReturn(config);

            // When
            int result = sysConfigService.getIntConfig(configKey, 0);

            // Then
            assertEquals(-456, result);
        }
    }
}
