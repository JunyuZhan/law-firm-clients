package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.AccessLogDTO;
import com.clientservice.application.service.AccessLogService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AccessLogController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccessLogController 单元测试")
class AccessLogControllerTest {

    @Mock
    private AccessLogService accessLogService;

    @Mock
    private MatterService matterService;

    @InjectMocks
    private AccessLogController accessLogController;

    private MockMvc mockMvc;
    private ClientMatter mockMatter;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accessLogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // 创建模拟项目
        mockMatter = createClientMatter("CS1234567890123456789");
    }

    @Nested
    @DisplayName("获取访问历史测试")
    class GetAccessHistoryTests {

        @Test
        @DisplayName("获取访问历史应该成功")
        void getAccessHistory_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            List<AccessLogDTO> logs = Arrays.asList(
                    createAccessLogDTO(),
                    createAccessLogDTO()
            );

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(accessLogService.getAccessHistory(eq(matterId), eq(mockMatter.getClientId()), isNull(), isNull(), isNull()))
                    .thenReturn(logs);

            // When & Then
            mockMvc.perform(get("/api/access-logs")
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, times(1)).getAccessHistory(eq(matterId), eq(mockMatter.getClientId()), isNull(), isNull(), isNull());
        }

        @Test
        @DisplayName("带时间范围和限制数量应该成功")
        void getAccessHistory_WithTimeRangeAndLimit_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            LocalDateTime startTime = LocalDateTime.now().minusDays(7);
            LocalDateTime endTime = LocalDateTime.now();
            Integer limit = 50;
            List<AccessLogDTO> logs = Arrays.asList(createAccessLogDTO());

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(accessLogService.getAccessHistory(eq(matterId), eq(mockMatter.getClientId()), eq(startTime), eq(endTime), eq(limit)))
                    .thenReturn(logs);

            // When & Then
            mockMvc.perform(get("/api/access-logs")
                            .param("matterId", matterId)
                            .param("token", token)
                            .param("startTime", startTime.toString())
                            .param("endTime", endTime.toString())
                            .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, times(1)).getAccessHistory(eq(matterId), eq(mockMatter.getClientId()), eq(startTime), eq(endTime), eq(limit));
        }

        @Test
        @DisplayName("项目ID不匹配应该返回403")
        void getAccessHistory_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String wrongMatterId = "CS9999999999999999999";
            String token = "test-token";

            ClientMatter wrongMatter = createClientMatter(wrongMatterId);
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            // When & Then
            mockMvc.perform(get("/api/access-logs")
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403))
                    .andExpect(jsonPath("$.message").value("项目ID不匹配"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, never()).getAccessHistory(anyString(), anyLong(), any(), any(), any());
        }

        @Test
        @DisplayName("无效的token应该返回错误")
        void getAccessHistory_WithInvalidToken_ShouldReturnError() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String invalidToken = "invalid-token";

            when(matterService.getMatterByToken(invalidToken)).thenThrow(
                    new BusinessException("404", "项目不存在或token无效"));

            // When & Then
            mockMvc.perform(get("/api/access-logs")
                            .param("matterId", matterId)
                            .param("token", invalidToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("404"));

            verify(matterService, times(1)).getMatterByToken(invalidToken);
            verify(accessLogService, never()).getAccessHistory(anyString(), anyLong(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("获取访问统计测试")
    class GetAccessStatisticsTests {

        @Test
        @DisplayName("获取访问统计应该成功")
        void getAccessStatistics_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            AccessLogService.AccessStatistics statistics = createAccessStatistics();

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(accessLogService.getAccessStatistics(eq(matterId), eq(mockMatter.getClientId()), isNull(), isNull()))
                    .thenReturn(statistics);

            // When & Then
            mockMvc.perform(get("/api/access-logs/statistics")
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(statistics.getTotalCount()))
                    .andExpect(jsonPath("$.data.uniqueIpCount").value(statistics.getUniqueIpCount()));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, times(1)).getAccessStatistics(eq(matterId), eq(mockMatter.getClientId()), isNull(), isNull());
        }

        @Test
        @DisplayName("带时间范围应该成功")
        void getAccessStatistics_WithTimeRange_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";
            LocalDateTime startTime = LocalDateTime.now().minusDays(30);
            LocalDateTime endTime = LocalDateTime.now();
            AccessLogService.AccessStatistics statistics = createAccessStatistics();

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            when(accessLogService.getAccessStatistics(eq(matterId), eq(mockMatter.getClientId()), eq(startTime), eq(endTime)))
                    .thenReturn(statistics);

            // When & Then
            mockMvc.perform(get("/api/access-logs/statistics")
                            .param("matterId", matterId)
                            .param("token", token)
                            .param("startTime", startTime.toString())
                            .param("endTime", endTime.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, times(1)).getAccessStatistics(eq(matterId), eq(mockMatter.getClientId()), eq(startTime), eq(endTime));
        }

        @Test
        @DisplayName("项目ID不匹配应该返回403")
        void getAccessStatistics_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String wrongMatterId = "CS9999999999999999999";
            String token = "test-token";

            ClientMatter wrongMatter = createClientMatter(wrongMatterId);
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            // When & Then
            mockMvc.perform(get("/api/access-logs/statistics")
                            .param("matterId", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value(403));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, never()).getAccessStatistics(anyString(), anyLong(), any(), any());
        }
    }

    /**
     * 创建测试用的ClientMatter
     */
    private ClientMatter createClientMatter(String matterId) {
        ClientMatter matter = new ClientMatter();
        matter.setId(matterId);
        matter.setClientId(2001L);
        matter.setClientName("测试客户");
        matter.setAccessToken("test-token");
        matter.setAccessUrl("http://localhost:8081/portal/matter/" + matterId + "?token=test-token");
        matter.setValidDays(30);
        matter.setStatus(ClientMatter.STATUS_ACTIVE);
        matter.setCreatedAt(LocalDateTime.now());
        matter.setUpdatedAt(LocalDateTime.now());
        matter.setDeleted(false);
        return matter;
    }

    /**
     * 创建测试用的AccessLogDTO
     */
    private AccessLogDTO createAccessLogDTO() {
        AccessLogDTO dto = new AccessLogDTO();
        dto.setMatterId("CS1234567890123456789");
        dto.setClientId(2001L);
        dto.setAccessTime(LocalDateTime.now());
        dto.setIpAddress("192.168.1.100");
        dto.setUserAgent("Mozilla/5.0");
        return dto;
    }

    /**
     * 创建测试用的AccessStatistics
     */
    private AccessLogService.AccessStatistics createAccessStatistics() {
        AccessLogService.AccessStatistics statistics = new AccessLogService.AccessStatistics();
        statistics.setTotalCount(10L);
        statistics.setUniqueIpCount(5L);
        return statistics;
    }
}
