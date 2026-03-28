package com.clientservice.application.service;

import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.infrastructure.persistence.mapper.DownloadLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * DownloadLogService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DownloadLogService 单元测试")
class DownloadLogServiceTest {

    @Mock
    private DownloadLogMapper downloadLogMapper;

    @Mock
    private CallbackService callbackService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private DownloadLogService downloadLogService;

    @BeforeEach
    void setUp() {
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        lenient().when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
    }

    @Nested
    @DisplayName("记录下载日志测试")
    class RecordDownloadTests {

        @Test
        @DisplayName("记录下载日志应该成功")
        void recordDownload_ShouldSuccess() {
            // Given
            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "判决书.pdf";
            String accessToken = "test-token";
            
            when(downloadLogMapper.insert(any(DownloadLog.class))).thenReturn(1);
            doNothing().when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When
            downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);

            // Then
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
            verify(callbackService, times(1)).callbackDownloadLog(any(DownloadLog.class));
        }

        @Test
        @DisplayName("回调失败不应该影响主流程")
        void recordDownload_CallbackFailure_ShouldNotAffectMainFlow() {
            // Given
            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "判决书.pdf";
            String accessToken = "test-token";
            
            when(downloadLogMapper.insert(any(DownloadLog.class))).thenReturn(1);
            doThrow(new RuntimeException("回调失败")).when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When & Then - 不应该抛出异常
            assertDoesNotThrow(() -> {
                downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);
            });

            // 验证日志记录成功
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
            verify(callbackService, times(1)).callbackDownloadLog(any(DownloadLog.class));
        }
    }

    @Nested
    @DisplayName("获取客户端IP地址测试")
    class GetClientIpAddressTests {

        @Test
        @DisplayName("应该优先使用X-Forwarded-For头")
        void getClientIpAddress_ShouldUseXForwardedFor() {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1");
            lenient().when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");
            lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "test.pdf";
            String accessToken = "test-token";

            when(downloadLogMapper.insert(any(DownloadLog.class))).thenAnswer(invocation -> {
                DownloadLog log = invocation.getArgument(0);
                assertEquals("203.0.113.1", log.getIpAddress());
                return 1;
            });
            doNothing().when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When
            downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);

            // Then
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
        }

        @Test
        @DisplayName("X-Forwarded-For为空时应该使用X-Real-IP")
        void getClientIpAddress_ShouldUseXRealIP_WhenXForwardedForIsNull() {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");
            lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "test.pdf";
            String accessToken = "test-token";

            when(downloadLogMapper.insert(any(DownloadLog.class))).thenAnswer(invocation -> {
                DownloadLog log = invocation.getArgument(0);
                assertEquals("192.168.1.100", log.getIpAddress());
                return 1;
            });
            doNothing().when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When
            downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);

            // Then
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
        }

        @Test
        @DisplayName("所有头都为空时应该使用RemoteAddr")
        void getClientIpAddress_ShouldUseRemoteAddr_WhenHeadersAreNull() {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "test.pdf";
            String accessToken = "test-token";

            when(downloadLogMapper.insert(any(DownloadLog.class))).thenAnswer(invocation -> {
                DownloadLog log = invocation.getArgument(0);
                assertEquals("127.0.0.1", log.getIpAddress());
                return 1;
            });
            doNothing().when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When
            downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);

            // Then
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
        }

        @Test
        @DisplayName("X-Forwarded-For包含多个IP时应该取第一个")
        void getClientIpAddress_ShouldTakeFirstIP_WhenXForwardedForHasMultipleIPs() {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 192.168.1.100");
            lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
            lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "test.pdf";
            String accessToken = "test-token";

            when(downloadLogMapper.insert(any(DownloadLog.class))).thenAnswer(invocation -> {
                DownloadLog log = invocation.getArgument(0);
                assertEquals("203.0.113.1", log.getIpAddress());
                return 1;
            });
            doNothing().when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When
            downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);

            // Then
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
        }

        @Test
        @DisplayName("X-Forwarded-For为unknown时应该使用下一个头")
        void getClientIpAddress_ShouldSkipUnknown_WhenXForwardedForIsUnknown() {
            // Given
            when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
            when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.100");
            lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String fileId = "CS1234567890123456789";
            String fileName = "test.pdf";
            String accessToken = "test-token";

            when(downloadLogMapper.insert(any(DownloadLog.class))).thenAnswer(invocation -> {
                DownloadLog log = invocation.getArgument(0);
                assertEquals("192.168.1.100", log.getIpAddress());
                return 1;
            });
            doNothing().when(callbackService).callbackDownloadLog(any(DownloadLog.class));

            // When
            downloadLogService.recordDownload(matterId, clientId, fileId, fileName, accessToken, request);

            // Then
            verify(downloadLogMapper, times(1)).insert(any(DownloadLog.class));
        }
    }
}
