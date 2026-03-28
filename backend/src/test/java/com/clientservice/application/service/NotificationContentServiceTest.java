package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationTemplate;
import com.clientservice.domain.entity.ClientMatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationContentService 单元测试")
class NotificationContentServiceTest {

    private final NotificationContentService notificationContentService =
            new NotificationContentService(new ObjectMapper());

    @Test
    @DisplayName("构建通知模板数据")
    void buildNotificationTemplate_ShouldMapMatterFields() {
        ClientMatter matter = ClientMatter.builder()
                .clientName("测试客户")
                .accessUrl("http://localhost/test")
                .validDays(30)
                .build();

        NotificationTemplate result = notificationContentService.buildNotificationTemplate(matter);

        assertEquals("测试客户的项目", result.getMatterName());
        assertEquals("测试客户", result.getClientName());
        assertEquals("http://localhost/test", result.getAccessUrl());
        assertEquals(30, result.getValidDays());
    }

    @Test
    @DisplayName("构建邮件内容时替换模板变量")
    void buildEmailContent_WithTemplateContent_ShouldReplaceVariables() {
        NotificationTemplate template = NotificationTemplate.builder()
                .matterName("测试项目")
                .accessUrl("http://localhost/test")
                .validDays(7)
                .build();
        com.clientservice.domain.entity.NotificationTemplate emailTemplate =
                new com.clientservice.domain.entity.NotificationTemplate();
        emailTemplate.setTemplateContent("项目：${matterName}");

        String result = notificationContentService.buildEmailContent(template, emailTemplate);

        assertEquals("项目：测试项目", result);
    }

    @Test
    @DisplayName("构建微信模板数据时替换JSON变量")
    void buildWechatTemplateData_WithJsonTemplate_ShouldReplaceVariables() {
        NotificationTemplate template = NotificationTemplate.builder()
                .matterName("测试项目")
                .accessUrl("http://localhost/test")
                .build();

        String result = notificationContentService.buildWechatTemplateData(
                "{\"keyword1\":{\"value\":\"${matterName}\"},\"keyword2\":{\"value\":\"${accessUrl}\"}}",
                template);

        assertTrue(result.contains("测试项目"));
        assertTrue(result.contains("http://localhost/test"));
    }
}
