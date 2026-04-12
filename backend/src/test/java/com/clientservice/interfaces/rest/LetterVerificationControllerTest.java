package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.LetterVerificationReceiveDTO;
import com.clientservice.application.service.ApiKeyService;
import com.clientservice.application.service.LetterVerificationService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("LetterVerificationController 单元测试")
class LetterVerificationControllerTest {

    @Mock
    private LetterVerificationService letterVerificationService;

    @Mock
    private ApiKeyService apiKeyService;

    @InjectMocks
    private LetterVerificationController letterVerificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ApiKey apiKey;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(letterVerificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        apiKey = new ApiKey();
        apiKey.setId(1001L);
        apiKey.setKeyName("law-firm-a");
    }

    @Test
    @DisplayName("接收函件验证数据时应传入当前来源 API Key")
    void receiveVerificationData_ShouldPassSourceApiKeyId() throws Exception {
        LetterVerificationReceiveDTO dto = LetterVerificationReceiveDTO.builder()
                .letterId(1L)
                .applicationNo("LTR-001")
                .verificationCode("code-123")
                .approvedAt(LocalDateTime.now())
                .build();

        when(apiKeyService.validateApiKey("Bearer valid-key")).thenReturn(apiKey);
        doNothing().when(letterVerificationService).receiveVerificationData(any(LetterVerificationReceiveDTO.class), eq(1001L));

        mockMvc.perform(post("/api/letter/verification/receive")
                        .header("Authorization", "Bearer valid-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(letterVerificationService).receiveVerificationData(any(LetterVerificationReceiveDTO.class), eq(1001L));
    }

    @Test
    @DisplayName("撤销其他来源函件时应返回403")
    void revokeVerification_WithMismatchedSource_ShouldReturnForbidden() throws Exception {
        when(apiKeyService.validateApiKey("Bearer valid-key")).thenReturn(apiKey);
        doThrow(new BusinessException(ErrorCode.FORBIDDEN, "无权撤销该函件验证信息"))
                .when(letterVerificationService)
                .revokeVerification(1L, 1001L);

        mockMvc.perform(delete("/api/letter/verification/{letterId}", 1L)
                        .header("Authorization", "Bearer valid-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN));
    }

    @Test
    @DisplayName("API Key 校验失败时不应调用业务服务")
    void receiveVerificationData_WithInvalidApiKey_ShouldNotInvokeService() throws Exception {
        LetterVerificationReceiveDTO dto = LetterVerificationReceiveDTO.builder()
                .letterId(1L)
                .applicationNo("LTR-001")
                .verificationCode("code-123")
                .build();

        when(apiKeyService.validateApiKey("Bearer invalid-key"))
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "API密钥无效"));

        mockMvc.perform(post("/api/letter/verification/receive")
                        .header("Authorization", "Bearer invalid-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED));

        verify(letterVerificationService, never()).receiveVerificationData(any(), eq(1001L));
    }
}
