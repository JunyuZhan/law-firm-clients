package com.clientservice.application.service;

import com.clientservice.common.util.UrlGenerator;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.CallbackTask;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.infrastructure.persistence.mapper.ApiKeyMapper;
import com.clientservice.infrastructure.persistence.mapper.CallbackTaskMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CallbackService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CallbackService 单元测试")
class CallbackServiceTest {

    @Mock
    private ClientMatterMapper matterMapper;

    @Mock
    private ApiKeyMapper apiKeyMapper;

    @Mock
    private CallbackTaskMapper callbackTaskMapper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SysConfigService sysConfigService;

    @Mock
    private UrlGenerator urlGenerator;

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private CallbackService callbackService;

    private ClientMatter testMatter;
    private AtomicReference<CallbackTask> insertedTask;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callbackService, "defaultLawFirmCallbackUrl", "http://example.com");
        ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", true);
        ReflectionTestUtils.setField(callbackService, "defaultOutboxMaxRetries", 3);
        ReflectionTestUtils.setField(callbackService, "outboxRetryDelayMinutes", 5);
        ReflectionTestUtils.setField(callbackService, "outboxBatchSize", 100);
        ReflectionTestUtils.setField(callbackService, "staleSendingMinutes", 10);
        ReflectionTestUtils.setField(callbackService, "objectMapper", new ObjectMapper());

        testMatter = new ClientMatter();
        testMatter.setId("CS1706860800000123456");
        testMatter.setLawFirmMatterId(456L);
        testMatter.setClientId(2001L);

        insertedTask = new AtomicReference<>();
        lenient().when(callbackTaskMapper.insert(any(CallbackTask.class))).thenAnswer(invocation -> {
            CallbackTask task = invocation.getArgument(0);
            task.setId(1L);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            insertedTask.set(task);
            return 1;
        });
        lenient().when(callbackTaskMapper.selectById(1L)).thenAnswer(invocation -> insertedTask.get());
        lenient().when(callbackTaskMapper.update(any(CallbackTask.class), any())).thenReturn(1);
        lenient().doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
    }

    @Nested
    @DisplayName("回调访问日志测试")
    class CallbackAccessLogTests {

        @Test
        @DisplayName("回调访问日志应该成功")
        void callbackAccessLog_ShouldSuccess() {
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

            callbackService.callbackAccessLog(accessLog);

            verify(matterMapper, times(2)).selectById("CS1706860800000123456");
            verify(callbackTaskMapper).insert(any(CallbackTask.class));
            verify(restTemplate).exchange(
                    eq("http://example.com/api/open/client/access-log"),
                    any(),
                    any(),
                    eq(String.class));
        }

        @Test
        @DisplayName("回调未启用时应该跳过")
        void callbackAccessLog_ShouldSkip_WhenDisabled() {
            ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", false);
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            callbackService.callbackAccessLog(accessLog);

            verify(matterMapper, never()).selectById(anyString());
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
            verify(callbackTaskMapper, never()).insert(any());
        }

        @Test
        @DisplayName("回调地址未配置时应该跳过")
        void callbackAccessLog_ShouldSkip_WhenUrlNotConfigured() {
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(sysConfigService.getConfigValue(eq("callback.law-firm-url"), anyString()))
                    .thenReturn("");
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(testMatter);

            callbackService.callbackAccessLog(accessLog);

            verify(callbackTaskMapper, never()).insert(any());
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("项目不存在时应该跳过")
        void callbackAccessLog_ShouldSkip_WhenMatterNotFound() {
            AccessLog accessLog = AccessLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            when(sysConfigService.getBooleanConfig(eq("callback.enabled"), anyBoolean())).thenReturn(true);
            when(matterMapper.selectById("CS1706860800000123456")).thenReturn(null);

            callbackService.callbackAccessLog(accessLog);

            verify(callbackTaskMapper, never()).insert(any());
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("回调失败不应该抛出异常")
        void callbackAccessLog_ShouldNotThrowException_WhenCallbackFails() {
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
                    .thenThrow(new RuntimeException("网络错误"));

            assertDoesNotThrow(() -> callbackService.callbackAccessLog(accessLog));
            verify(callbackTaskMapper).insert(any(CallbackTask.class));
        }

        @Test
        @DisplayName("应该使用系统配置优先于默认配置")
        void callbackAccessLog_ShouldUseSystemConfig_WhenAvailable() {
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

            callbackService.callbackAccessLog(accessLog);

            verify(restTemplate).exchange(
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

            verify(restTemplate).exchange(
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
                    eq(HttpMethod.POST),
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

            callbackService.callbackDownloadLog(downloadLog);

            verify(callbackTaskMapper).insert(any(CallbackTask.class));
            verify(restTemplate).exchange(
                    eq("http://example.com/api/open/client/download-log"),
                    any(),
                    any(),
                    eq(String.class));
        }

        @Test
        @DisplayName("回调未启用时应该跳过")
        void callbackDownloadLog_ShouldSkip_WhenDisabled() {
            ReflectionTestUtils.setField(callbackService, "defaultCallbackEnabled", false);
            DownloadLog downloadLog = DownloadLog.builder()
                    .matterId("CS1706860800000123456")
                    .clientId(2001L)
                    .build();

            callbackService.callbackDownloadLog(downloadLog);

            verify(callbackTaskMapper, never()).insert(any());
            verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
        }

        @Test
        @DisplayName("回调失败不应该抛出异常")
        void callbackDownloadLog_ShouldNotThrowException_WhenCallbackFails() {
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
                    .thenThrow(new RuntimeException("网络错误"));

            assertDoesNotThrow(() -> callbackService.callbackDownloadLog(downloadLog));
            verify(callbackTaskMapper).insert(any(CallbackTask.class));
        }

        @Test
        @DisplayName("回调数据应该包含正确的字段")
        void callbackDownloadLog_ShouldIncludeCorrectFields() {
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

            callbackService.callbackDownloadLog(downloadLog);

            verify(restTemplate).exchange(
                    anyString(),
                    any(),
                    argThat(entity -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) ((HttpEntity<?>) entity).getBody();
                        assertNotNull(data);
                        assertEquals(456, data.get("matterId"));
                        assertEquals(2001, data.get("clientId"));
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

            verify(restTemplate).exchange(
                    eq("http://example.org/api/open/client/download-log"),
                    any(),
                    any(),
                    eq(String.class));
        }
    }
}
