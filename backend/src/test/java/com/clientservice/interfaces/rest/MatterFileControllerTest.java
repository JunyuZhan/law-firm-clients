package com.clientservice.interfaces.rest;

import com.clientservice.application.service.ApiKeyService;
import com.clientservice.application.service.FileService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatterFileController 单元测试")
class MatterFileControllerTest {

    @Mock
    private ApiKeyService apiKeyService;

    @Mock
    private FileService fileService;

    @InjectMocks
    private MatterFileController matterFileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(matterFileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("删除回调在来源归属匹配时应该成功")
    void deleteFile_ShouldValidateSourceOwnership() throws Exception {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(1001L);
        apiKey.setKeyName("law-firm-a");

        when(apiKeyService.validateApiKey("Bearer valid-key")).thenReturn(apiKey);
        doNothing().when(fileService).deleteFileForSource("file-123", 1001L);

        mockMvc.perform(post("/api/files/delete")
                        .header("Authorization", "Bearer valid-key")
                        .param("fileId", "file-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200));

        verify(fileService, times(1)).deleteFileForSource("file-123", 1001L);
    }

    @Test
    @DisplayName("删除回调在来源归属不匹配时应该返回403")
    void deleteFile_WithMismatchedSource_ShouldReturnForbidden() throws Exception {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(1001L);
        apiKey.setKeyName("law-firm-a");

        when(apiKeyService.validateApiKey("Bearer valid-key")).thenReturn(apiKey);
        org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.FORBIDDEN, "无权删除该文件"))
                .when(fileService)
                .deleteFileForSource("file-123", 1001L);

        mockMvc.perform(post("/api/files/delete")
                        .header("Authorization", "Bearer valid-key")
                        .param("fileId", "file-123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN));

        verify(fileService, times(1)).deleteFileForSource("file-123", 1001L);
    }

    @Test
    @DisplayName("文件ID为空时不应调用删除")
    void deleteFile_WithoutFileId_ShouldReturnBadRequest() throws Exception {
        ApiKey apiKey = new ApiKey();
        apiKey.setId(1001L);
        apiKey.setKeyName("law-firm-a");

        when(apiKeyService.validateApiKey("Bearer valid-key")).thenReturn(apiKey);

        mockMvc.perform(post("/api/files/delete")
                        .header("Authorization", "Bearer valid-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST));

        verify(fileService, never()).deleteFileForSource(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong());
    }
}
