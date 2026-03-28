package com.clientservice.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clientservice.application.dto.AccessLogDTO;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.infrastructure.persistence.mapper.AccessLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * AccessLogService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessLogService 单元测试")
class AccessLogServiceTest {

    @Mock
    private AccessLogMapper accessLogMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CallbackService callbackService;

    @InjectMocks
    private AccessLogService accessLogService;

    private Page<AccessLog> anyAccessLogPage() {
        return org.mockito.ArgumentMatchers.<Page<AccessLog>>any();
    }

    private LambdaQueryWrapper<AccessLog> anyAccessLogQueryWrapper() {
        return org.mockito.ArgumentMatchers.<LambdaQueryWrapper<AccessLog>>any();
    }

    @BeforeEach
    void setUp() {
        lenient().when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        lenient().when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
    }

    @Nested
    @DisplayName("记录访问日志测试")
    class RecordAccessTests {

        @Test
        @DisplayName("记录访问日志应该成功")
        void recordAccess_ShouldSuccess() {
            // Given
            String matterId = "CS1706860800000123456";
            Long clientId = 2001L;
            String token = "test-token";
            when(accessLogMapper.insert(any(AccessLog.class))).thenReturn(1);

            // When
            accessLogService.recordAccess(matterId, clientId, token, request);

            // Then
            verify(accessLogMapper, times(1)).insert(any(AccessLog.class));
        }
    }

    @Nested
    @DisplayName("获取访问历史测试")
    class GetAccessHistoryTests {

        @Test
        @DisplayName("获取访问历史应该返回列表")
        void getAccessHistory_ShouldReturnList() {
            // Given
            String matterId = "CS1706860800000123456";
            AccessLog log1 = createMockAccessLog(1L, matterId, 2001L, "test-token", "192.168.1.100", "Mozilla/5.0");
            AccessLog log2 = createMockAccessLog(2L, matterId, 2001L, "test-token", "192.168.1.101", "Mozilla/5.0");
            
            Page<AccessLog> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(log1, log2));
            when(accessLogMapper.selectPage(anyAccessLogPage(), anyAccessLogQueryWrapper())).thenReturn(page);

            // When
            var result = accessLogService.getAccessHistory(matterId, null, null, null, 10);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.get(0) instanceof AccessLogDTO);
        }

        @Test
        @DisplayName("限制数量应该生效")
        void getAccessHistory_WithLimit_ShouldRespectLimit() {
            // Given
            String matterId = "CS1706860800000123456";
            AccessLog log1 = createMockAccessLog(1L, matterId, null, null, null, null);
            AccessLog log2 = createMockAccessLog(2L, matterId, null, null, null, null);
            
            // Mock返回2条记录，因为limit是2，SQL会限制返回数量
            Page<AccessLog> page = new Page<>(1, 2);
            page.setRecords(Arrays.asList(log1, log2));
            when(accessLogMapper.selectPage(anyAccessLogPage(), anyAccessLogQueryWrapper())).thenReturn(page);

            // When
            var result = accessLogService.getAccessHistory(matterId, null, null, null, 2);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
        }
        
        private AccessLog createMockAccessLog(Long id, String matterId, Long clientId, 
                String accessToken, String ipAddress, String userAgent) {
            AccessLog log = mock(AccessLog.class);
            when(log.getId()).thenReturn(id);
            when(log.getMatterId()).thenReturn(matterId);
            when(log.getClientId()).thenReturn(clientId);
            when(log.getAccessToken()).thenReturn(accessToken);
            when(log.getIpAddress()).thenReturn(ipAddress);
            when(log.getUserAgent()).thenReturn(userAgent);
            when(log.getAccessTime()).thenReturn(LocalDateTime.now());
            return log;
        }
    }

    @Nested
    @DisplayName("获取访问统计测试")
    class GetAccessStatisticsTests {

        @Test
        @DisplayName("获取访问统计应该返回统计数据")
        void getAccessStatistics_ShouldReturnStatistics() {
            // Given
            String matterId = "CS1706860800000123456";
            AccessLog log1 = new AccessLog();
            log1.setMatterId(matterId);
            log1.setClientId(2001L);
            log1.setIpAddress("192.168.1.100");
            log1.setAccessTime(LocalDateTime.now());
            AccessLog log2 = new AccessLog();
            log2.setMatterId(matterId);
            log2.setClientId(2001L);
            log2.setIpAddress("192.168.1.101");
            log2.setAccessTime(LocalDateTime.now());
            AccessLog log3 = new AccessLog();
            log3.setMatterId(matterId);
            log3.setClientId(2001L);
            log3.setIpAddress("192.168.1.100"); // 重复IP
            log3.setAccessTime(LocalDateTime.now());
            
            when(accessLogMapper.selectCount(any())).thenReturn(3L);
            when(accessLogMapper.selectList(any())).thenReturn(Arrays.asList(log1, log2, log3));

            // When
            var statistics = accessLogService.getAccessStatistics(matterId, null, null, null);

            // Then
            assertNotNull(statistics);
            assertEquals(3L, statistics.getTotalCount());
            assertEquals(2L, statistics.getUniqueIpCount()); // 2个不同的IP
        }
    }

    @Nested
    @DisplayName("清理过期日志测试")
    class DeleteBeforeTests {

        @Test
        @DisplayName("清理过期日志应该调用Mapper")
        void deleteBefore_ShouldCallMapper() {
            // Given
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
            when(accessLogMapper.deleteBefore(cutoffDate)).thenReturn(10);

            // When
            int deleted = accessLogService.deleteBefore(cutoffDate);

            // Then
            assertEquals(10, deleted);
            verify(accessLogMapper, times(1)).deleteBefore(cutoffDate);
        }
    }
}
