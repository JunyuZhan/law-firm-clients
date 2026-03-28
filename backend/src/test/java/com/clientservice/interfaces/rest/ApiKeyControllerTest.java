package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ApiKeyDTO;
import com.clientservice.application.service.ApiKeyService;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyController 单元测试")
class ApiKeyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ApiKeyService apiKeyService;

    @InjectMocks
    private ApiKeyController apiKeyController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(apiKeyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Nested
    @DisplayName("获取API密钥列表测试")
    class GetApiKeyListTests {

        @Test
        @DisplayName("获取列表成功")
        void getApiKeyList_ShouldReturnSuccess() throws Exception {
            // Given
            ApiKeyDTO apiKey1 = ApiKeyDTO.builder().id(1L).keyName("Key 1").build();
            ApiKeyDTO apiKey2 = ApiKeyDTO.builder().id(2L).keyName("Key 2").build();
            List<ApiKeyDTO> apiKeys = Arrays.asList(apiKey1, apiKey2);

            when(apiKeyService.getApiKeyList(anyBoolean(), anyString(), anyInt())).thenReturn(apiKeys);

            // When & Then
            mockMvc.perform(get("/api/admin/api-keys")
                    .param("enabled", "true")
                    .param("lawFirmName", "Test")
                    .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[1].id").value(2));
            
            verify(apiKeyService).getApiKeyList(true, "Test", 10);
        }
    }

    @Nested
    @DisplayName("获取API密钥详情测试")
    class GetApiKeyByIdTests {

        @Test
        @DisplayName("获取详情成功")
        void getApiKeyById_ShouldReturnSuccess() throws Exception {
            // Given
            ApiKeyDTO apiKey = ApiKeyDTO.builder().id(1L).keyName("Test Key").build();
            when(apiKeyService.getApiKeyById(1L)).thenReturn(apiKey);

            // When & Then
            mockMvc.perform(get("/api/admin/api-keys/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.keyName").value("Test Key"));
        }
    }

    @Nested
    @DisplayName("创建API密钥测试")
    class CreateApiKeyTests {

        @Test
        @DisplayName("创建API密钥成功")
        void createApiKey_ShouldReturnSuccess() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("keyName", "New Key");
            request.put("lawFirmName", "Test Firm");
            request.put("expiresAt", "2026-12-31T23:59:59");

            ApiKeyDTO apiKey = ApiKeyDTO.builder()
                    .id(1L)
                    .keyName("New Key")
                    .lawFirmName("Test Firm")
                    .build();

            when(apiKeyService.createApiKey(anyString(), anyString(), any(LocalDateTime.class))).thenReturn(apiKey);

            // When & Then
            mockMvc.perform(post("/api/admin/api-keys")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.keyName").value("New Key"));
            
            verify(apiKeyService).createApiKey(eq("New Key"), eq("Test Firm"), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("缺少名称创建失败")
        void createApiKey_WithoutName_ShouldFail() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("lawFirmName", "Test Firm");
            // 缺少必填字段 keyName，参数验证会失败

            // When & Then
            // 参数验证失败时，@Valid 注解会在 Controller 方法执行前返回 400 Bad Request
            mockMvc.perform(post("/api/admin/api-keys")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest()); // 参数验证失败返回 400
        }
    }

    @Nested
    @DisplayName("更新API密钥测试")
    class UpdateApiKeyTests {

        @Test
        @DisplayName("更新API密钥成功")
        void updateApiKey_ShouldReturnSuccess() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("keyName", "Updated Key");
            request.put("enabled", true);

            ApiKeyDTO apiKey = ApiKeyDTO.builder()
                    .id(1L)
                    .keyName("Updated Key")
                    .enabled(true)
                    .build();

            when(apiKeyService.updateApiKey(anyLong(), anyString(), any(), anyBoolean(), any())).thenReturn(apiKey);

            // When & Then
            mockMvc.perform(put("/api/admin/api-keys/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.keyName").value("Updated Key"));
            
            verify(apiKeyService).updateApiKey(eq(1L), eq("Updated Key"), any(), eq(true), any());
        }
    }

    @Nested
    @DisplayName("删除API密钥测试")
    class DeleteApiKeyTests {

        @Test
        @DisplayName("删除API密钥成功")
        void deleteApiKey_ShouldReturnSuccess() throws Exception {
            // Given
            doNothing().when(apiKeyService).deleteApiKey(1L);

            // When & Then
            mockMvc.perform(delete("/api/admin/api-keys/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
            
            verify(apiKeyService).deleteApiKey(1L);
        }
    }
}
