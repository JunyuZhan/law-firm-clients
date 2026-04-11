package com.clientservice.application.service;

import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CallbackService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CallbackService 单元测试")
class CallbackServiceTest {

    @Mock
    private ClientMatterMapper matterMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private CallbackService callbackService;

    private ClientMatter testMatter;

    @BeforeEach
    void setUp() {
        // 设置默认回调地址
        ReflectionTestUtils.setField(callbackService, "defaultLawFirmCallbackUrl", "http://example.com");
        ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", true);

        // 创建测试项目
        testMatter = new ClientMatter();
        testMatter.setId("CS1706860800000123456");
        testMatter.setLawFirmMatterId(456L);
        testMatter.setClientId(2001L);
    }

    @Nested
    @DisplayName("回调访问日志测试")
    class CallbackAccessLogTests {

        @Test
        @DisplayName("回调访问日志应该成功")
        void callbackAccessLog_ShouldSuccess() {
            // Given
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .accessToken("test-token")
                    .ipAddress("192.168.1.100")
                    .userAgent("Mozilla/5.0")
                    .accessTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.com");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            // When
            callbackService.callbackAccessLog(accessLog);

            // Then
            verify(matterMapper, times(1)).selectById("CS1706860800000123456");
            verify(restTemplate, times(1)).exchange(
                     eq("http://example.com/api/open/client/access-log"),
                    any(),
                    any(),
                    eq(String.class));
        }

        @Test
        @DisplayName("回调未启用时应该跳过")
        void callbackAccessLog_ShouldSkip_WhenDisabled() {
            // Given
            ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", false);
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            // When
            callbackService.callbackAccessLog(accessLog);

            // Then
            verify(matterMapper, never()).selectById(anyString());
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("回调地址未配置时应该跳过")
        void callbackAccessLog_ShouldSkip_WhenUrlNotConfigured() {
            // Given
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);

            // When
            callbackService.callbackAccessLog(accessLog);

            // Then
            verify(matterMapper, times(1)).selectById("CS1706860800000123456");
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("项目不存在时应该跳过")
        void callbackAccessLog_ShouldSkip_WhenMatterNotFound() {
            // Given
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(null);

            // When
            callbackService.callbackAccessLog(accessLog);

            // Then
            verify(matterMapper, times(1)).selectById("CS1706860800000123456");
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("回调失败不应该抛出异常")
        void callbackAccessLog_ShouldNotThrowException_WhenCallbackFails() {
            // Given
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .accessTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.com");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new RestClientException("网络错误"));

            // When & Then - 不应该抛出异常
            assertDoesNotThrow(() -> {
                callbackService.callbackAccessLog(accessLog);
            });
        }

        @Test
        @DisplayName("应该使用系统配置优先于默认配置")
        void callbackAccessLog_ShouldUseSystemConfig_WhenAvailable() {
            // Given
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .accessTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), eq("http://example.com")))
                    .thenReturn("http://example.org");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            // When
            callbackService.callbackAccessLog(accessLog);

            // Then
            verify(restTemplate, times(1)).exchange(
                     eq("http://example.org/api/open/client/access-log"),
                    any(),
                    any(),
                    eq(String.class));
        }

        @Test
        @DisplayName("回调地址已含/api时不应重复拼接")
        void callbackAccessLog_ShouldNotDuplicateApiPrefix() {
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .accessTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.org/api");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            callbackService.callbackAccessLog(accessLog);

            verify(restTemplate, times(1)).exchange(
                    eq("http://example.org/api/open/client/access-log"),
                    any(),
                    any(),
                    eq(String.class));
        }

        @Test
        @DisplayName("回调访问日志应携带签名与防重放请求头")
        void callbackAccessLog_ShouldIncludeSignatureHeaders() {
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .accessTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.com");
            when(sysConfigService.getConfigValue(eq("callback.api-key"), isNull()))
                    .thenReturn("test-secret");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            callbackService.callbackAccessLog(accessLog);

            @SuppressWarnings("rawtypes")
            ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).exchange(
                    eq("http://example.com/api/open/client/access-log"),
                    eq(org.springframework.http.HttpMethod.POST),
                    entityCaptor.capture(),
                    eq(String.class));

            HttpEntity<?> entity = entityCaptor.getValue();
            assertNotNull(entity.getHeaders().getFirst("X-Callback-Key"));
            assertNotNull(entity.getHeaders().getFirst("X-Callback-Timestamp"));
            assertNotNull(entity.getHeaders().getFirst("X-Callback-Nonce"));
            assertNotNull(entity.getHeaders().getFirst("X-Callback-Signature"));
        }
    }

    @Nested
    @DisplayName("回调下载日志测试")
    class CallbackDownloadLogTests {

        @Test
        @DisplayName("回调下载日志应该成功")
        void callbackDownloadLog_ShouldSuccess() {
            // Given
            DownloadLog downloadLog = DownloadLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .fileId("CS1234567890123456789")
                    .fileName("判决书.pdf")
                    .downloadTime(LocalDateTime.now())
                    .ipAddress("192.168.1.100")
                    .userAgent("Mozilla/5.0")
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.com");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            // When
            callbackService.callbackDownloadLog(downloadLog);

            // Then
            verify(matterMapper, times(1)).selectById("CS1706860800000123456");
            verify(restTemplate, times(1)).exchange(
                     eq("http://example.com/api/open/client/download-log"),
                    any(),
                    any(),
                    eq(String.class));
        }

        @Test
        @DisplayName("回调未启用时应该跳过")
        void callbackDownloadLog_ShouldSkip_WhenDisabled() {
            // Given
            ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", false);
            DownloadLog downloadLog = DownloadLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            // When
            callbackService.callbackDownloadLog(downloadLog);

            // Then
            verify(matterMapper, never()).selectById(anyString());
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("回调失败不应该抛出异常")
        void callbackDownloadLog_ShouldNotThrowException_WhenCallbackFails() {
            // Given
            DownloadLog downloadLog = DownloadLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .fileId("CS1234567890123456789")
                    .downloadTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.com");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new RestClientException("网络错误"));

            // When & Then - 不应该抛出异常
            assertDoesNotThrow(() -> {
                callbackService.callbackDownloadLog(downloadLog);
            });
        }

        @Test
        @DisplayName("回调数据应该包含正确的字段")
        void callbackDownloadLog_ShouldIncludeCorrectFields() {
            // Given
            DownloadLog downloadLog = DownloadLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .fileId("CS1234567890123456789")
                    .fileName("判决书.pdf")
                    .downloadTime(LocalDateTime.now())
                    .ipAddress("192.168.1.100")
                    .userAgent("Mozilla/5.0")
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.com");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            // When
            callbackService.callbackDownloadLog(downloadLog);

            // Then
            verify(restTemplate, times(1)).exchange(
                    anyString(),
                    any(),
                    argThat(entity -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) ((org.springframework.http.HttpEntity<?>) entity).getBody();
                        assertNotNull(data);
                        assertEquals(456L, data.get("matterId")); // 律所系统的项目ID
                        assertEquals(2001L, data.get("clientId"));
                        assertEquals("CS1234567890123456789", data.get("fileId"));
                        assertEquals("判决书.pdf", data.get("fileName"));
                        assertEquals("DOWNLOAD", data.get("eventType"));
                        return true;
                    }),
                    eq(String.class));
        }

        @Test
        @DisplayName("下载日志回调地址已含/api时不应重复拼接")
        void callbackDownloadLog_ShouldNotDuplicateApiPrefix() {
            DownloadLog downloadLog = DownloadLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .fileId("CS1234567890123456789")
                    .downloadTime(LocalDateTime.now())
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("http://example.org/api");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

            callbackService.callbackDownloadLog(downloadLog);

            verify(restTemplate, times(1)).exchange(
                    eq("http://example.org/api/open/client/download-log"),
                    any(),
                    any(),
                    eq(String.class));
        }
    }
}
