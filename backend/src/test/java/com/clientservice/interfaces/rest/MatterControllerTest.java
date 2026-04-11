package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.application.service.ApiKeyService;
import com.clientservice.application.service.MatterService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MatterController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatterController 单元测试")
class MatterControllerTest {

    @Mock
    private MatterService matterService;

    @Mock
    private ApiKeyService apiKeyService;

    @InjectMocks
    private MatterController matterController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ApiKey mockApiKey;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(matterController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // 创建模拟API密钥
        mockApiKey = new ApiKey();
        mockApiKey.setId(1L);
        mockApiKey.setKeyName("测试API密钥");
        mockApiKey.setLawFirmName("测试律师事务所");
        mockApiKey.setEnabled(true);
    }

    @Nested
    @DisplayName("接收项目数据测试")
    class ReceiveMatterDataTests {

        @Test
        @DisplayName("接收项目数据应该成功")
        void receiveMatterData_ShouldSuccess() throws Exception {
            // Given
            MatterReceiveRequest request = createMatterReceiveRequest();
            MatterReceiveResponse response = createMatterReceiveResponse();

            when(apiKeyService.validateApiKey(anyString())).thenReturn(mockApiKey);
            when(matterService.receiveMatterData(any(MatterReceiveRequest.class), any(HttpServletRequest.class), any(ApiKey.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/matter/receive")
                            .header("Authorization", "Bearer test-api-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(response.getId()))
                    .andExpect(jsonPath("$.data.accessUrl").value(response.getAccessUrl()));

            verify(apiKeyService, times(1)).validateApiKey("Bearer test-api-key");
            verify(matterService, times(1)).receiveMatterData(any(MatterReceiveRequest.class), any(HttpServletRequest.class), any(ApiKey.class));
        }

        @Test
        @DisplayName("缺少Authorization头应该返回500（参数缺失）")
        void receiveMatterData_WithoutAuthorization_ShouldReturn500() throws Exception {
            // Given
            MatterReceiveRequest request = createMatterReceiveRequest();
            // 注意：在 MockMvc 单元测试中，缺少 @RequestHeader 会抛出 MissingRequestHeaderException
            // 由于 GlobalExceptionHandler 没有专门处理这个异常，会返回 500 Internal Server Error
            // 这与实际生产环境的行为可能不同（生产环境可能有额外的配置）

            // When & Then
            mockMvc.perform(post("/api/matter/receive")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError()); // 500

            verify(apiKeyService, never()).validateApiKey(anyString());
            verify(matterService, never()).receiveMatterData(any(), any(), any());
        }

        @Test
        @DisplayName("无效的API密钥应该返回401")
        void receiveMatterData_WithInvalidApiKey_ShouldReturn401() throws Exception {
            // Given
            MatterReceiveRequest request = createMatterReceiveRequest();
            when(apiKeyService.validateApiKey("Bearer invalid-key")).thenThrow(
                    new BusinessException("401", "API密钥无效"));

            // When & Then
            mockMvc.perform(post("/api/matter/receive")
                            .header("Authorization", "Bearer invalid-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("401"));

            verify(apiKeyService, times(1)).validateApiKey("Bearer invalid-key");
            verify(matterService, never()).receiveMatterData(any(), any(), any());
        }

        @Test
        @DisplayName("请求参数验证失败应该返回400")
        void receiveMatterData_WithInvalidRequest_ShouldReturn400() throws Exception {
            // Given
            MatterReceiveRequest invalidRequest = new MatterReceiveRequest();
            // 缺少必填字段

            // When & Then
            // 参数验证失败时，@Valid会在Controller方法执行前抛出异常，所以apiKeyService不会被调用
            mockMvc.perform(post("/api/matter/receive")
                            .header("Authorization", "Bearer test-api-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest()); // 验证失败会返回400

            // 参数验证失败时，Controller方法不会执行，所以这些服务不会被调用
            verify(apiKeyService, never()).validateApiKey(anyString());
            verify(matterService, never()).receiveMatterData(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("获取项目详情测试")
    class GetMatterByIdTests {

        @Test
        @DisplayName("获取项目详情应该成功")
        void getMatterById_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            ClientMatter matter = createClientMatter(matterId);

            when(apiKeyService.validateApiKey(anyString())).thenReturn(mockApiKey);
            when(matterService.getMatterById(matterId)).thenReturn(matter);

            // When & Then
            mockMvc.perform(get("/api/matter/{id}", matterId)
                            .header("Authorization", "Bearer test-api-key"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(matterId))
                    .andExpect(jsonPath("$.data.clientName").value(matter.getClientName()));

            verify(apiKeyService, times(1)).validateApiKey("Bearer test-api-key");
            verify(matterService, times(1)).getMatterById(matterId);
        }

        @Test
        @DisplayName("项目不存在应该返回404")
        void getMatterById_WithNonExistentMatter_ShouldReturn404() throws Exception {
            // Given
            String matterId = "non-existent";
            when(apiKeyService.validateApiKey(anyString())).thenReturn(mockApiKey);
            when(matterService.getMatterById(matterId)).thenThrow(
                    new BusinessException("404", "项目不存在"));

            // When & Then
            mockMvc.perform(get("/api/matter/{id}", matterId)
                            .header("Authorization", "Bearer test-api-key"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.code").value("404"));

            verify(apiKeyService, times(1)).validateApiKey("Bearer test-api-key");
            verify(matterService, times(1)).getMatterById(matterId);
        }

        @Test
        @DisplayName("缺少Authorization头应该返回500（参数缺失）")
        void getMatterById_WithoutAuthorization_ShouldReturn500() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            // 注意：在 MockMvc 单元测试中，缺少 @RequestHeader 会抛出 MissingRequestHeaderException
            // 由于 GlobalExceptionHandler 没有专门处理这个异常，会返回 500 Internal Server Error

            // When & Then
            mockMvc.perform(get("/api/matter/{id}", matterId))
                    .andExpect(status().isInternalServerError()); // 500

            verify(apiKeyService, never()).validateApiKey(anyString());
            verify(matterService, never()).getMatterById(anyString());
        }
    }

    /**
     * 创建测试用的MatterReceiveRequest
     */
    private MatterReceiveRequest createMatterReceiveRequest() {
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(2001L);
        request.setClientName("测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 1001);
        matterData.put("matterName", "测试项目");
        request.setMatterData(matterData);
        
        request.setScopes(Arrays.asList("MATTER_INFO", "MATTER_PROGRESS"));
        request.setValidDays(30);
        return request;
    }

    /**
     * 创建测试用的MatterReceiveResponse
     */
    private MatterReceiveResponse createMatterReceiveResponse() {
        MatterReceiveResponse response = new MatterReceiveResponse();
        response.setId("CS1234567890123456789");
        response.setAccessUrl("http://localhost:8081/portal/matter/CS1234567890123456789?token=test-token");
        return response;
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
