package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.SysConfigDTO;
import com.clientservice.application.service.SysConfigService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
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
class SysConfigControllerTest {

    @Mock
    private SysConfigService sysConfigService;

    @InjectMocks
    private SysConfigController sysConfigController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sysConfigController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("获取配置列表")
    class GetConfigListTests {

        @Test
        @DisplayName("获取配置列表成功")
        void getConfigList_ShouldReturnSuccess() throws Exception {
            // Given
            SysConfigDTO config1 = SysConfigDTO.builder()
                    .id(1L)
                    .configKey("test.key1")
                    .configValue("value1")
                    .build();
            SysConfigDTO config2 = SysConfigDTO.builder()
                    .id(2L)
                    .configKey("test.key2")
                    .configValue("value2")
                    .build();
            List<SysConfigDTO> configList = Arrays.asList(config1, config2);

            when(sysConfigService.getConfigList(any(), any(), any())).thenReturn(configList);

            // When & Then
            mockMvc.perform(get("/api/admin/config")
                    .param("configKey", "test")
                    .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].configKey").value("test.key1"));

            verify(sysConfigService).getConfigList("test", null, 10);
        }
    }

    @Nested
    @DisplayName("根据ID获取配置")
    class GetConfigByIdTests {

        @Test
        @DisplayName("获取配置详情成功")
        void getConfigById_ShouldReturnSuccess() throws Exception {
            // Given
            SysConfigDTO config = SysConfigDTO.builder()
                    .id(1L)
                    .configKey("test.key")
                    .configValue("value")
                    .build();

            when(sysConfigService.getConfigById(1L)).thenReturn(config);

            // When & Then
            mockMvc.perform(get("/api/admin/config/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.configKey").value("test.key"));

            verify(sysConfigService).getConfigById(1L);
        }
    }

    @Nested
    @DisplayName("创建或更新配置")
    class SaveConfigTests {

        @Test
        @DisplayName("创建配置成功")
        void saveConfig_ShouldReturnSuccess() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("configKey", "new.key");
            request.put("configValue", "new value");
            request.put("configType", "STRING");
            request.put("description", "desc");

            SysConfigDTO config = SysConfigDTO.builder()
                    .id(1L)
                    .configKey("new.key")
                    .configValue("new value")
                    .build();

            when(sysConfigService.saveConfig(anyString(), anyString(), anyString(), anyString())).thenReturn(config);

            // When & Then
            mockMvc.perform(post("/api/admin/config")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.configKey").value("new.key"));

            verify(sysConfigService).saveConfig("new.key", "new value", "STRING", "desc");
        }

        @Test
        @DisplayName("配置键为空时返回错误")
        void saveConfig_WhenKeyIsEmpty_ShouldReturnError() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("configValue", "value");

            // When & Then
            mockMvc.perform(post("/api/admin/config")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("500")) // Assuming Result.error returns 500 or check specific error code
                    .andExpect(jsonPath("$.message").value("配置键不能为空"));
        }
    }

    @Nested
    @DisplayName("更新配置")
    class UpdateConfigTests {

        @Test
        @DisplayName("更新配置成功")
        void updateConfig_ShouldReturnSuccess() throws Exception {
            // Given
            Map<String, Object> request = new HashMap<>();
            request.put("configValue", "updated value");
            request.put("configType", "STRING");
            request.put("description", "updated desc");

            SysConfigDTO config = SysConfigDTO.builder()
                    .id(1L)
                    .configKey("key")
                    .configValue("updated value")
                    .build();

            when(sysConfigService.updateConfig(eq(1L), anyString(), anyString(), anyString())).thenReturn(config);

            // When & Then
            mockMvc.perform(put("/api/admin/config/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.configValue").value("updated value"));

            verify(sysConfigService).updateConfig(1L, "updated value", "STRING", "updated desc");
        }
    }

    @Nested
    @DisplayName("删除配置")
    class DeleteConfigTests {

        @Test
        @DisplayName("删除配置成功")
        void deleteConfig_ShouldReturnSuccess() throws Exception {
            // Given
            doNothing().when(sysConfigService).deleteConfig(1L);

            // When & Then
            mockMvc.perform(delete("/api/admin/config/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(sysConfigService).deleteConfig(1L);
        }
    }
}
