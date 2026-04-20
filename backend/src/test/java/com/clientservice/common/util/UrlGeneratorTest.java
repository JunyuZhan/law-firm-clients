package com.clientservice.common.util;

import com.clientservice.application.service.SysConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UrlGenerator 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UrlGenerator 单元测试")
class UrlGeneratorTest {

    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private UrlGenerator urlGenerator;

    @BeforeEach
    void setUp() {
        // 设置默认baseUrl
        ReflectionTestUtils.setField(urlGenerator, "defaultBaseUrl", "http://localhost:8081");
        ReflectionTestUtils.setField(urlGenerator, "allowRequestDerivedBaseUrl", false);
        // Mock SysConfigService 返回null，使用默认值
        when(sysConfigService.getConfigValue(eq("system.base-url"), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    @DisplayName("生成访问链接应该包含项目ID和token")
    void generateAccessUrl_ShouldContainMatterIdAndToken() {
        // Given
        when(sysConfigService.getConfigValue("system.base-url", "http://localhost:8081"))
                .thenReturn("http://localhost:8081");
        String matterId = "CS1706860800000123456";
        String token = "test-token-12345678901234567890123456789012";

        // When
        String url = urlGenerator.generateAccessUrl(matterId, token);

        // Then
        assertNotNull(url);
        assertTrue(url.contains(matterId));
        assertTrue(url.contains(token));
        assertTrue(url.startsWith("http://localhost:8081"));
        assertTrue(url.contains("/portal/matter/"));
    }

    @Test
    @DisplayName("生成访问链接格式正确")
    void generateAccessUrl_ShouldHaveCorrectFormat() {
        // Given
        when(sysConfigService.getConfigValue("system.base-url", "http://localhost:8081"))
                .thenReturn("http://example.com");
        String matterId = "CS1706860800000123456";
        String token = "test-token";

        // When
        String url = urlGenerator.generateAccessUrl(matterId, token);

        // Then
        String expected = "http://example.com/portal/matter/CS1706860800000123456?token=test-token";
        assertEquals(expected, url);
    }

    @Test
    @DisplayName("使用自定义baseUrl应该正确生成链接")
    void generateAccessUrl_WithCustomBaseUrl_ShouldUseCustomUrl() {
        // Given
        ReflectionTestUtils.setField(urlGenerator, "defaultBaseUrl", "https://custom-domain.com");
        when(sysConfigService.getConfigValue("system.base-url", "https://custom-domain.com"))
                .thenReturn("https://custom-domain.com");
        String matterId = "CS1706860800000123456";
        String token = "test-token";

        // When
        String url = urlGenerator.generateAccessUrl(matterId, token);

        // Then
        assertTrue(url.startsWith("https://custom-domain.com"));
    }

    @Test
    @DisplayName("从系统配置读取baseUrl应该优先使用")
    void generateAccessUrl_ShouldUseSystemConfigBaseUrl() {
        // Given
        when(sysConfigService.getConfigValue("system.base-url", "http://localhost:8081"))
                .thenReturn("http://192.168.50.10:8081");
        String matterId = "CS1706860800000123456";
        String token = "test-token";

        // When
        String url = urlGenerator.generateAccessUrl(matterId, token);

        // Then
        assertTrue(url.startsWith("http://192.168.50.10:8081"));
        verify(sysConfigService, times(1)).getConfigValue("system.base-url", "http://localhost:8081");
    }

    @Test
    @DisplayName("系统配置不存在时应该使用默认值")
    void generateAccessUrl_WhenSystemConfigNotExists_ShouldUseDefault() {
        // Given
        when(sysConfigService.getConfigValue("system.base-url", "http://localhost:8081"))
                .thenReturn("http://localhost:8081");
        String matterId = "CS1706860800000123456";
        String token = "test-token";

        // When
        String url = urlGenerator.generateAccessUrl(matterId, token);

        // Then
        assertTrue(url.startsWith("http://localhost:8081"));
    }

    @Test
    @DisplayName("auto 模式默认不应信任请求头生成外链")
    void generateAccessUrl_WithAutoBaseUrl_ShouldFallbackToDefaultWhenHeaderTrustDisabled() {
        when(sysConfigService.getConfigValue("system.base-url", "http://localhost:8081"))
                .thenReturn("auto");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "evil.example.com");
        request.addHeader("X-Forwarded-Proto", "https");

        String url = urlGenerator.generateAccessUrl("CS1706860800000123456", "test-token", request);

        assertTrue(url.startsWith("http://localhost:8081"));
        assertFalse(url.startsWith("https://evil.example.com"));
    }

    @Test
    @DisplayName("启用请求头推导时应拒绝不安全Host")
    void generateAccessUrl_WithUnsafeForwardedHost_ShouldFallbackToDefault() {
        ReflectionTestUtils.setField(urlGenerator, "allowRequestDerivedBaseUrl", true);
        when(sysConfigService.getConfigValue("system.base-url", "http://localhost:8081"))
                .thenReturn("auto");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Host", "localhost:8081");
        request.addHeader("X-Forwarded-Proto", "https");
        request.addHeader("X-Forwarded-Host", "evil.example.com/path");

        String url = urlGenerator.generateAccessUrl("CS1706860800000123456", "test-token", request);

        assertTrue(url.startsWith("http://localhost:8081"));
    }
}
