package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.notification.wechat.WechatAccessTokenService;
import com.clientservice.infrastructure.notification.wechat.WechatApiClient;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WechatNotificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WechatNotificationService 单元测试")
class WechatNotificationServiceTest {

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @Mock
    private WechatAccessTokenService accessTokenService;

    @Mock
    private WechatApiClient wechatApiClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private com.clientservice.application.service.SysConfigService sysConfigService;

    @Mock
    private com.clientservice.application.service.CallbackService callbackService;

    @InjectMocks
    private WechatNotificationService wechatNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wechatNotificationService, "wechatEnabledDefault", true);
        // Mock SysConfigService 返回默认值
        lenient().when(sysConfigService.getBooleanConfig(eq("notification.wechat.enabled"), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Nested
    @DisplayName("发送微信通知测试")
    class SendWechatTests {

        @Test
        @DisplayName("发送微信通知应该成功")
        void sendWechat_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String openId = "test-openid-123";
            String templateId = "template-id-123";
            String templateData = "{\"first\":{\"value\":\"项目信息已更新\"}}";

            when(accessTokenService.isConfigured()).thenReturn(true);
            when(accessTokenService.getAccessToken()).thenReturn("test-access-token");
            when(wechatApiClient.postJson(anyString(), anyString())).thenReturn(createWechatSuccessResponse());
            when(objectMapper.readTree(anyString())).thenReturn(createSuccessJsonNode());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When
            NotificationRecord result = wechatNotificationService.sendWechat(
                    matterId, lawFirmMatterId, clientName, clientId, openId, templateId, templateData);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.TYPE_WECHAT, result.getNotificationType());
            assertEquals(openId, result.getRecipient());
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            assertNotNull(result.getSentAt());
            verify(accessTokenService, times(1)).getAccessToken();
            verify(wechatApiClient, times(1)).postJson(anyString(), anyString());
            verify(notificationRecordMapper, times(1)).insert(any(NotificationRecord.class));
            verify(notificationRecordMapper, times(1)).updateById(any(NotificationRecord.class));
        }

        @Test
        @DisplayName("微信服务未启用时应该返回null")
        void sendWechat_WhenDisabled_ShouldReturnNull() {
            // Given
            ReflectionTestUtils.setField(wechatNotificationService, "wechatEnabledDefault", false);
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String openId = "test-openid-123";
            String templateId = "template-id-123";
            String templateData = "{\"first\":{\"value\":\"项目信息已更新\"}}";

            // When
            NotificationRecord result = wechatNotificationService.sendWechat(
                    matterId, lawFirmMatterId, clientName, clientId, openId, templateId, templateData);

            // Then
            assertNull(result);
            verify(notificationRecordMapper, never()).insert(any(NotificationRecord.class));
        }

        @Test
        @DisplayName("微信发送失败应该更新状态为失败")
        void sendWechat_WithSendFailure_ShouldUpdateStatusToFailed() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String openId = "test-openid-123";
            String templateId = "template-id-123";
            String templateData = "{\"first\":{\"value\":\"项目信息已更新\"}}";

            when(accessTokenService.isConfigured()).thenReturn(true);
            when(accessTokenService.getAccessToken()).thenReturn("test-access-token");
            when(wechatApiClient.postJson(anyString(), anyString())).thenReturn(createWechatFailureResponse());
            when(objectMapper.readTree(anyString())).thenReturn(createFailureJsonNode());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> wechatNotificationService.sendWechat(matterId, lawFirmMatterId, clientName, clientId, openId, templateId, templateData));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getCode());
            assertTrue(exception.getMessage().contains("微信通知发送失败"));

            verify(notificationRecordMapper, times(1)).updateById(argThat(record ->
                    NotificationRecord.STATUS_FAILED.equals(((NotificationRecord) record).getStatus())));
        }

        @Test
        @DisplayName("AccessToken过期时应该清除缓存")
        void sendWechat_WithExpiredToken_ShouldClearCache() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String openId = "test-openid-123";
            String templateId = "template-id-123";
            String templateData = "{\"first\":{\"value\":\"项目信息已更新\"}}";

            when(accessTokenService.isConfigured()).thenReturn(true);
            when(accessTokenService.getAccessToken()).thenReturn("expired-token");
            when(wechatApiClient.postJson(anyString(), anyString())).thenReturn(createWechatTokenExpiredResponse());
            when(objectMapper.readTree(anyString())).thenReturn(createTokenExpiredJsonNode());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When & Then
            assertThrows(BusinessException.class,
                    () -> wechatNotificationService.sendWechat(matterId, lawFirmMatterId, clientName, clientId, openId, templateId, templateData));

            // Then - 验证清除缓存被调用
            verify(accessTokenService, times(1)).clearAccessTokenCache();
        }

        @Test
        @DisplayName("未配置微信时应该模拟发送成功")
        void sendWechat_WhenNotConfigured_ShouldSimulateSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String openId = "test-openid-123";
            String templateId = "template-id-123";
            String templateData = "{\"first\":{\"value\":\"项目信息已更新\"}}";

            when(accessTokenService.isConfigured()).thenReturn(false);
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When
            NotificationRecord result = wechatNotificationService.sendWechat(
                    matterId, lawFirmMatterId, clientName, clientId, openId, templateId, templateData);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            verify(accessTokenService, never()).getAccessToken();
            verify(wechatApiClient, never()).postJson(anyString(), anyString());
        }

        @Test
        @DisplayName("无效的模板数据应该抛出异常")
        void sendWechat_WithInvalidTemplateData_ShouldThrowException() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String openId = "test-openid-123";
            String templateId = "template-id-123";
            String invalidTemplateData = "invalid-json";

            when(accessTokenService.isConfigured()).thenReturn(true);
            when(accessTokenService.getAccessToken()).thenReturn("test-access-token");
            // buildTemplateMessageRequest会先调用readTree验证JSON，这里模拟失败
            when(objectMapper.readTree(eq(invalidTemplateData))).thenThrow(new RuntimeException("Invalid JSON"));
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });

            // When & Then
            assertThrows(BusinessException.class,
                    () -> wechatNotificationService.sendWechat(matterId, lawFirmMatterId, clientName, clientId, openId, templateId, invalidTemplateData));
        }
    }

    // 辅助方法：创建微信成功响应
    private String createWechatSuccessResponse() {
        return "{\"errcode\":0,\"errmsg\":\"ok\",\"msgid\":123456789}";
    }

    // 辅助方法：创建微信失败响应
    private String createWechatFailureResponse() {
        return "{\"errcode\":40001,\"errmsg\":\"invalid access_token\"}";
    }

    // 辅助方法：创建AccessToken过期响应
    private String createWechatTokenExpiredResponse() {
        return "{\"errcode\":40014,\"errmsg\":\"invalid access_token\"}";
    }

    // 辅助方法：创建成功JsonNode
    private JsonNode createSuccessJsonNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree("{\"errcode\":0,\"errmsg\":\"ok\"}");
    }

    // 辅助方法：创建失败JsonNode
    private JsonNode createFailureJsonNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree("{\"errcode\":40001,\"errmsg\":\"invalid access_token\"}");
    }

    // 辅助方法：创建Token过期JsonNode
    private JsonNode createTokenExpiredJsonNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree("{\"errcode\":40014,\"errmsg\":\"invalid access_token\"}");
    }
}
