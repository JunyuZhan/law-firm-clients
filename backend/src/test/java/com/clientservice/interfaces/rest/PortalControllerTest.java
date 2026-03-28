package com.clientservice.interfaces.rest;

import com.clientservice.application.service.AccessLogService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PortalController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PortalController 单元测试")
class PortalControllerTest {

    @Mock
    private MatterService matterService;

    @Mock
    private AccessLogService accessLogService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PortalController portalController;

    private MockMvc mockMvc;
    private ClientMatter mockMatter;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(portalController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // 创建模拟项目
        mockMatter = createClientMatter("CS1234567890123456789");
        
        // 模拟HttpServletRequest - 使用lenient避免UnnecessaryStubbingException
        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.100");
        lenient().when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
    }

    @Nested
    @DisplayName("获取项目详情测试")
    class GetMatterDetailTests {

        @Test
        @DisplayName("获取项目详情应该成功")
        void getMatterDetail_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            doNothing().when(accessLogService).recordAccess(anyString(), anyLong(), anyString(), any(HttpServletRequest.class));

            // When & Then
            mockMvc.perform(get("/portal/api/matter/{id}", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(matterId))
                    .andExpect(jsonPath("$.data.clientName").value(mockMatter.getClientName()));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, times(1)).recordAccess(eq(matterId), eq(mockMatter.getClientId()), eq(token), any(HttpServletRequest.class));
        }

        @Test
        @DisplayName("项目ID不匹配应该返回403")
        void getMatterDetail_WithMismatchedMatterId_ShouldReturn403() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String wrongMatterId = "CS9999999999999999999";
            String token = "test-token";

            ClientMatter wrongMatter = createClientMatter(wrongMatterId);
            when(matterService.getMatterByToken(token)).thenReturn(wrongMatter);

            // When & Then
            mockMvc.perform(get("/portal/api/matter/{id}", matterId)
                            .param("token", token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("403"))
                    .andExpect(jsonPath("$.message").value("项目ID不匹配"));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, never()).recordAccess(anyString(), anyLong(), anyString(), any(HttpServletRequest.class));
        }

        @Test
        @DisplayName("无效的token应该返回错误")
        void getMatterDetail_WithInvalidToken_ShouldReturnError() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String invalidToken = "invalid-token";

            when(matterService.getMatterByToken(invalidToken)).thenThrow(
                    new BusinessException("404", "项目不存在或token无效"));

            // When & Then
            mockMvc.perform(get("/portal/api/matter/{id}", matterId)
                            .param("token", invalidToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("404"));

            verify(matterService, times(1)).getMatterByToken(invalidToken);
            verify(accessLogService, never()).recordAccess(anyString(), anyLong(), anyString(), any(HttpServletRequest.class));
        }

        @Test
        @DisplayName("缺少token参数应该返回400")
        void getMatterDetail_WithoutToken_ShouldReturn400() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";

            // When & Then
            mockMvc.perform(get("/portal/api/matter/{id}", matterId))
                    .andExpect(status().isBadRequest()); // 缺少required参数会返回400

            verify(matterService, never()).getMatterByToken(anyString());
            verify(accessLogService, never()).recordAccess(anyString(), anyLong(), anyString(), any(HttpServletRequest.class));
        }

        @Test
        @DisplayName("访问日志记录失败不应该影响主流程")
        void getMatterDetail_WithAccessLogFailure_ShouldStillReturnSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            String token = "test-token";

            when(matterService.getMatterByToken(token)).thenReturn(mockMatter);
            doThrow(new RuntimeException("访问日志记录失败"))
                    .when(accessLogService).recordAccess(anyString(), anyLong(), anyString(), any(HttpServletRequest.class));

            // When & Then - 访问日志失败不应该影响主流程
            mockMvc.perform(get("/portal/api/matter/{id}", matterId)
                            .param("token", token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(matterId));

            verify(matterService, times(1)).getMatterByToken(token);
            verify(accessLogService, times(1)).recordAccess(eq(matterId), eq(mockMatter.getClientId()), eq(token), any(HttpServletRequest.class));
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
}
