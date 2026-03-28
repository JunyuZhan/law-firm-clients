package com.clientservice.application.service;

import com.clientservice.application.dto.ContactInfoExtractResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationContactResolver 单元测试")
class NotificationContactResolverTest {

    private final NotificationContactResolver notificationContactResolver =
            new NotificationContactResolver(new ObjectMapper());

    @Test
    @DisplayName("解析项目数据成功")
    void parseMatterData_WithValidJson_ShouldReturnMap() {
        Map<String, Object> result = notificationContactResolver.parseMatterData(
                "{\"phone\":\"13800138000\",\"email\":\"test@example.com\"}");

        assertEquals("13800138000", result.get("phone"));
        assertEquals("test@example.com", result.get("email"));
    }

    @Test
    @DisplayName("解析非法项目数据返回空Map")
    void parseMatterData_WithInvalidJson_ShouldReturnEmptyMap() {
        Map<String, Object> result = notificationContactResolver.parseMatterData("{invalid}");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("按候选字段提取手机号")
    void extractPhoneNumber_WithAlternativeField_ShouldReturnValue() {
        ContactInfoExtractResult result = notificationContactResolver.extractPhoneNumber(
                Map.of("mobilePhone", "13800138000"));

        assertTrue(result.isFound());
        assertEquals("13800138000", result.getValue());
        assertArrayEquals(
                new String[]{"phone", "phoneNumber", "mobile", "mobilePhone", "contactPhone"},
                result.getSearchedFieldNames());
    }
}
