package com.clientservice.application.service;

import com.clientservice.application.dto.NotificationTemplate;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.notification.aliyun.AliyunSmsClient;
import com.clientservice.infrastructure.notification.tencent.TencentSmsClient;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
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
 * SmsNotificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SmsNotificationService 单元测试")
class SmsNotificationServiceTest {

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @Mock
    private AliyunSmsClient aliyunSmsClient;

    @Mock
    private TencentSmsClient tencentSmsClient;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper templateMapper;

    @Mock
    private SysConfigService sysConfigService;

    @Mock
    private CallbackService callbackService;

    @InjectMocks
    private SmsNotificationService smsNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(smsNotificationService, "smsEnabledDefault", true);
        ReflectionTestUtils.setField(smsNotificationService, "providerDefault", "aliyun");
    }

    @Nested
    @DisplayName("发送短信测试")
    class SendSmsTests {

        @Test
        @DisplayName("发送短信应该成功（阿里云）")
        void sendSms_WithAliyun_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";

            // 创建通知模板DTO（包含变量数据）
            NotificationTemplate template = NotificationTemplate.builder()
                    .matterName("测试项目")
                    .clientName("测试客户")
                    .accessUrl("http://localhost:8081/portal?token=test")
                    .validDays(30)
                    .build();

            // 创建数据库模板Entity（可以为null，表示使用默认模板）
            com.clientservice.domain.entity.NotificationTemplate smsTemplate = null;

            when(aliyunSmsClient.isConfigured()).thenReturn(true);
            when(aliyunSmsClient.sendSms(anyString(), any(), any(), anyString())).thenReturn(createAliyunSuccessResponse());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"content\":\"测试短信内容\"}");

            // When
            NotificationRecord result = smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.TYPE_SMS, result.getNotificationType());
            assertEquals(phoneNumber, result.getRecipient());
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            assertNotNull(result.getSentAt());
            verify(aliyunSmsClient, times(1)).sendSms(eq(phoneNumber), any(), any(), anyString());
            verify(notificationRecordMapper, times(1)).insert(any(NotificationRecord.class));
            verify(notificationRecordMapper, times(1)).updateById(any(NotificationRecord.class));
        }

        @Test
        @DisplayName("发送短信应该成功（腾讯云）")
        void sendSms_WithTencent_ShouldSuccess() throws Exception {
            // Given
            ReflectionTestUtils.setField(smsNotificationService, "providerDefault", "tencent");
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";

            // 创建通知模板DTO
            NotificationTemplate template = NotificationTemplate.builder()
                    .matterName("测试项目")
                    .clientName("测试客户")
                    .accessUrl("http://localhost:8081/portal?token=test")
                    .validDays(30)
                    .build();

            com.clientservice.domain.entity.NotificationTemplate smsTemplate = null;

            when(tencentSmsClient.isConfigured()).thenReturn(true);
            when(tencentSmsClient.sendSms(anyString(), any(), any(), any(String[].class))).thenReturn(createTencentSuccessResponse());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When
            NotificationRecord result = smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            verify(tencentSmsClient, times(1)).sendSms(eq(phoneNumber), any(), any(), any(String[].class));
        }

        @Test
        @DisplayName("短信服务未启用时应该返回null")
        void sendSms_WhenDisabled_ShouldReturnNull() {
            // Given
            ReflectionTestUtils.setField(smsNotificationService, "smsEnabledDefault", false);
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";
            
            NotificationTemplate template = NotificationTemplate.builder()
                    .matterName("测试项目")
                    .clientName("测试客户")
                    .build();
            com.clientservice.domain.entity.NotificationTemplate smsTemplate = null;

            // When
            NotificationRecord result = smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate);

            // Then
            assertNull(result);
            verify(notificationRecordMapper, never()).insert(any(NotificationRecord.class));
        }

        @Test
        @DisplayName("短信发送失败应该更新状态为失败")
        void sendSms_WithSendFailure_ShouldUpdateStatusToFailed() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";

            NotificationTemplate template = NotificationTemplate.builder()
                    .matterName("测试项目")
                    .build();
            com.clientservice.domain.entity.NotificationTemplate smsTemplate = null;

            when(aliyunSmsClient.isConfigured()).thenReturn(true);
            when(aliyunSmsClient.sendSms(anyString(), any(), any(), anyString())).thenReturn(createAliyunFailureResponse());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"content\":\"测试短信内容\"}");

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate));

            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getCode());
            assertTrue(exception.getMessage().contains("短信发送失败"));

            verify(notificationRecordMapper, times(1)).updateById(argThat(record ->
                    NotificationRecord.STATUS_FAILED.equals(((NotificationRecord) record).getStatus())));
        }

        @Test
        @DisplayName("未配置提供商时应该模拟发送成功")
        void sendSms_WhenProviderNotConfigured_ShouldSimulateSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";

            NotificationTemplate template = NotificationTemplate.builder()
                    .matterName("测试项目")
                    .build();
            com.clientservice.domain.entity.NotificationTemplate smsTemplate = null;

            when(aliyunSmsClient.isConfigured()).thenReturn(false);
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When
            NotificationRecord result = smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            verify(aliyunSmsClient, never()).sendSms(anyString(), anyString());
        }
        @Test
        @DisplayName("使用数据库模板发送短信应该成功")
        @SuppressWarnings("unchecked")
        void sendSms_WithDatabaseTemplate_ShouldSuccess() throws Exception {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";

            NotificationTemplate template = NotificationTemplate.builder()
                    .matterName("测试项目")
                    .clientName("测试客户")
                    .build();
            
            // 模拟数据库模板
            com.clientservice.domain.entity.NotificationTemplate smsTemplate = new com.clientservice.domain.entity.NotificationTemplate();
            smsTemplate.setTemplateContent("尊敬的${clientName}，您的项目${matterName}有更新。");
            
            when(aliyunSmsClient.isConfigured()).thenReturn(true);
            when(aliyunSmsClient.sendSms(anyString(), any(), any(), anyString())).thenReturn(createAliyunSuccessResponse());
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            
            // 验证内容替换逻辑，这里我们不需要Mock objectMapper，因为Service中会用到它
            // 但是我们需要Mock buildSmsContent中用到的objectMapper.writeValueAsString如果不走模板逻辑的话
            // 当使用数据库模板时，buildSmsContent会进行变量替换
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"content\":\"测试内容\"}");
            // Mock readValue to throw exception for non-JSON content (triggers fallback)
            when(objectMapper.readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                    .thenThrow(new RuntimeException("Not JSON"));
            
            // When
            NotificationRecord result = smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            // 验证使用了数据库模板内容进行替换
            assertTrue(result.getContent().contains("测试客户"));
            assertTrue(result.getContent().contains("测试项目"));
        }

        @Test
        @DisplayName("腾讯云发送失败应该抛出异常")
        void sendSms_WithTencentFailure_ShouldThrowException() throws Exception {
            // Given
            ReflectionTestUtils.setField(smsNotificationService, "providerDefault", "tencent");
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String phoneNumber = "13800138000";
            
            NotificationTemplate template = NotificationTemplate.builder().build();
            com.clientservice.domain.entity.NotificationTemplate smsTemplate = null;

            when(tencentSmsClient.isConfigured()).thenReturn(true);
            // 模拟腾讯云发送失败
            com.tencentcloudapi.sms.v20210111.models.SendStatus status = new com.tencentcloudapi.sms.v20210111.models.SendStatus();
            status.setCode("Failed");
            status.setMessage("Send failed");
            com.tencentcloudapi.sms.v20210111.models.SendSmsResponse response = new com.tencentcloudapi.sms.v20210111.models.SendSmsResponse();
            response.setSendStatusSet(new com.tencentcloudapi.sms.v20210111.models.SendStatus[]{status});
            
            when(tencentSmsClient.sendSms(anyString(), any(), any(), any(String[].class))).thenReturn(response);
            
            when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                record.setId(1L);
                return null;
            });
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> smsNotificationService.sendSms(matterId, lawFirmMatterId, clientName, clientId, phoneNumber, template, smsTemplate));
            
            assertTrue(exception.getMessage().contains("短信发送失败"));
        }
    }
    private com.aliyun.dysmsapi20170525.models.SendSmsResponse createAliyunSuccessResponse() {
        com.aliyun.dysmsapi20170525.models.SendSmsResponseBody body =
                new com.aliyun.dysmsapi20170525.models.SendSmsResponseBody();
        body.setCode("OK");
        body.setMessage("success");
        com.aliyun.dysmsapi20170525.models.SendSmsResponse response =
                new com.aliyun.dysmsapi20170525.models.SendSmsResponse();
        response.setBody(body);
        return response;
    }

    // 辅助方法：创建阿里云失败响应
    private com.aliyun.dysmsapi20170525.models.SendSmsResponse createAliyunFailureResponse() {
        com.aliyun.dysmsapi20170525.models.SendSmsResponseBody body =
                new com.aliyun.dysmsapi20170525.models.SendSmsResponseBody();
        body.setCode("FAIL");
        body.setMessage("发送失败");
        com.aliyun.dysmsapi20170525.models.SendSmsResponse response =
                new com.aliyun.dysmsapi20170525.models.SendSmsResponse();
        response.setBody(body);
        return response;
    }

    // 辅助方法：创建腾讯云成功响应
    private com.tencentcloudapi.sms.v20210111.models.SendSmsResponse createTencentSuccessResponse() {
        com.tencentcloudapi.sms.v20210111.models.SendStatus status =
                new com.tencentcloudapi.sms.v20210111.models.SendStatus();
        status.setCode("Ok");
        status.setMessage("send success");
        com.tencentcloudapi.sms.v20210111.models.SendSmsResponse response =
                new com.tencentcloudapi.sms.v20210111.models.SendSmsResponse();
        response.setSendStatusSet(new com.tencentcloudapi.sms.v20210111.models.SendStatus[]{status});
        return response;
    }
}
