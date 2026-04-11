package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.NotificationTemplateCreateRequest;
import com.clientservice.application.dto.NotificationTemplateDTO;
import com.clientservice.application.dto.NotificationTemplateUpdateRequest;
import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.NotificationTemplateService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
class NotificationTemplateControllerTest {

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @InjectMocks
    private NotificationTemplateController templateController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(templateController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("获取模板列表")
    class GetTemplateListTests {

        @Test
        @DisplayName("获取模板列表成功")
        void getTemplateList_ShouldReturnSuccess() throws Exception {
            // Given
            NotificationTemplateDTO template1 = NotificationTemplateDTO.builder()
                    .id(1L)
                    .templateName("Test Template 1")
                    .build();
            NotificationTemplateDTO template2 = NotificationTemplateDTO.builder()
                    .id(2L)
                    .templateName("Test Template 2")
                    .build();
            List<NotificationTemplateDTO> templateList = Arrays.asList(template1, template2);

            when(templateService.getTemplateList(any(), any(), any(), any())).thenReturn(templateList);
            doNothing().when(adminAuthorizationService).requireSuperAdmin();

            // When & Then
            mockMvc.perform(get("/api/admin/notification-templates")
                    .param("templateType", "SMS")
                    .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].templateName").value("Test Template 1"));

            verify(templateService).getTemplateList("SMS", null, null, 10);
        }
    }

    @Nested
    @DisplayName("根据ID获取模板")
    class GetTemplateByIdTests {

        @Test
        @DisplayName("获取模板详情成功")
        void getTemplateById_ShouldReturnSuccess() throws Exception {
            // Given
            NotificationTemplateDTO template = NotificationTemplateDTO.builder()
                    .id(1L)
                    .templateName("Test Template")
                    .build();

            when(templateService.getTemplateById(1L)).thenReturn(template);
            doNothing().when(adminAuthorizationService).requireSuperAdmin();

            // When & Then
            mockMvc.perform(get("/api/admin/notification-templates/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.templateName").value("Test Template"));

            verify(templateService).getTemplateById(1L);
        }
    }

    @Nested
    @DisplayName("创建模板")
    class CreateTemplateTests {

        @Test
        @DisplayName("创建模板成功")
        void createTemplate_ShouldReturnSuccess() throws Exception {
            // Given
            NotificationTemplateCreateRequest request = new NotificationTemplateCreateRequest();
            request.setTemplateName("New Template");
            request.setTemplateType("SMS");
            request.setTemplateCode("SMS_123456");
            request.setTemplateContent("Your code is {code}");
            request.setProvider("ALIYUN");
            request.setEnabled(true);

            NotificationTemplateDTO template = NotificationTemplateDTO.builder()
                    .id(1L)
                    .templateName("New Template")
                    .build();

            when(templateService.createTemplate(
                    anyString(), anyString(), anyString(), anyString(), 
                    any(), anyString(), any(), anyBoolean(), any()))
                    .thenReturn(template);
            doNothing().when(adminAuthorizationService).requireSuperAdmin();

            // When & Then
            mockMvc.perform(post("/api/admin/notification-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.templateName").value("New Template"));

            verify(templateService).createTemplate(
                    "New Template", "SMS", "SMS_123456", "Your code is {code}", 
                    null, "ALIYUN", null, true, null);
        }

        @Test
        @DisplayName("模板名称为空时返回错误")
        void createTemplate_WhenNameIsEmpty_ShouldReturnError() throws Exception {
            // Given
            NotificationTemplateCreateRequest request = new NotificationTemplateCreateRequest();
            request.setTemplateType("SMS");

            // When & Then
            mockMvc.perform(post("/api/admin/notification-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("模板名称不能为空"));
        }
    }

    @Nested
    @DisplayName("更新模板")
    class UpdateTemplateTests {

        @Test
        @DisplayName("更新模板成功")
        void updateTemplate_ShouldReturnSuccess() throws Exception {
            // Given
            NotificationTemplateUpdateRequest request = new NotificationTemplateUpdateRequest();
            request.setTemplateName("Updated Template");
            request.setTemplateContent("Updated Content");
            request.setEnabled(false);

            NotificationTemplateDTO template = NotificationTemplateDTO.builder()
                    .id(1L)
                    .templateName("Updated Template")
                    .build();

            when(templateService.updateTemplate(
                    eq(1L), anyString(), anyString(), any(), anyBoolean(), any()))
                    .thenReturn(template);
            doNothing().when(adminAuthorizationService).requireSuperAdmin();

            // When & Then
            mockMvc.perform(put("/api/admin/notification-templates/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.templateName").value("Updated Template"));

            verify(templateService).updateTemplate(
                    1L, "Updated Template", "Updated Content", null, false, null);
        }
    }

    @Nested
    @DisplayName("删除模板")
    class DeleteTemplateTests {

        @Test
        @DisplayName("删除模板成功")
        void deleteTemplate_ShouldReturnSuccess() throws Exception {
            // Given
            doNothing().when(templateService).deleteTemplate(1L);
            doNothing().when(adminAuthorizationService).requireSuperAdmin();

            // When & Then
            mockMvc.perform(delete("/api/admin/notification-templates/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(templateService).deleteTemplate(1L);
        }
    }
}
