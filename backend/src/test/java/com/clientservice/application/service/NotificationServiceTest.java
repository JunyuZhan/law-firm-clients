package com.clientservice.application.service;

import com.clientservice.application.dto.ContactInfoExtractResult;
import com.clientservice.application.dto.NotificationTemplate;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 单元测试")
class NotificationServiceTest {

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private WechatNotificationService wechatNotificationService;

    @Mock
    private NotificationChannelConfigService channelConfigService;

    @Mock
    private NotificationContactResolver contactResolver;

    @Mock
    private NotificationContentService notificationContentService;

    @Mock
    private NotificationTemplateMapper templateMapper;

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @InjectMocks
    private NotificationService notificationService;

    private ClientMatter testMatter;
    private NotificationTemplate testTemplate;
    private ContactInfoExtractResult phoneResult;
    private ContactInfoExtractResult emailResult;
    private ContactInfoExtractResult wechatResult;

    @BeforeEach
    void setUp() {
        testMatter = ClientMatter.builder()
                .id("CS1234567890123456789")
                .clientId(2001L)
                .lawFirmMatterId(1001L)
                .clientName("测试客户")
                .accessUrl("http://localhost:8081/portal?token=test-token")
                .validDays(30)
                .matterData("{\"phone\":\"13800138000\",\"email\":\"test@example.com\",\"wechatOpenId\":\"test-openid\"}")
                .build();

        testTemplate = NotificationTemplate.builder()
                .matterName("测试客户的项目")
                .clientName("测试客户")
                .accessUrl("http://localhost:8081/portal?token=test-token")
                .validDays(30)
                .build();

        phoneResult = ContactInfoExtractResult.builder()
                .value("13800138000")
                .searchedFieldNames(new String[]{"phone"})
                .build();
        emailResult = ContactInfoExtractResult.builder()
                .value("test@example.com")
                .searchedFieldNames(new String[]{"email"})
                .build();
        wechatResult = ContactInfoExtractResult.builder()
                .value("test-openid")
                .searchedFieldNames(new String[]{"wechatOpenId"})
                .build();

        lenient().when(contactResolver.parseMatterData(anyString())).thenReturn(Map.of(
                "phone", "13800138000",
                "email", "test@example.com",
                "wechatOpenId", "test-openid"));
        lenient().when(contactResolver.extractPhoneNumber(anyMap())).thenReturn(phoneResult);
        lenient().when(contactResolver.extractEmail(anyMap())).thenReturn(emailResult);
        lenient().when(contactResolver.extractWechatOpenId(anyMap())).thenReturn(wechatResult);

        lenient().when(notificationContentService.buildNotificationTemplate(testMatter)).thenReturn(testTemplate);
        lenient().when(notificationContentService.buildEmailContent(eq(testTemplate), nullable(com.clientservice.domain.entity.NotificationTemplate.class)))
                .thenReturn("邮件内容");
        lenient().when(notificationContentService.buildWechatTemplateData(nullable(String.class), eq(testTemplate)))
                .thenReturn("微信内容");

        lenient().when(channelConfigService.isEmailEnabled()).thenReturn(true);
        lenient().when(channelConfigService.isSmsEnabled()).thenReturn(true);
        lenient().when(channelConfigService.isWechatEnabled()).thenReturn(true);
        lenient().when(channelConfigService.getSmsProvider()).thenReturn("aliyun");
        lenient().when(channelConfigService.getWechatTemplateId()).thenReturn("template-id");

        lenient().when(templateMapper.selectEnabledByTypeAndProvider(
                com.clientservice.domain.entity.NotificationTemplate.TYPE_EMAIL, null))
                .thenReturn(Collections.emptyList());
        lenient().when(templateMapper.selectEnabledByTypeAndProvider(
                com.clientservice.domain.entity.NotificationTemplate.TYPE_SMS, "aliyun"))
                .thenReturn(Collections.emptyList());
        lenient().when(templateMapper.selectEnabledByTypeAndProvider(
                com.clientservice.domain.entity.NotificationTemplate.TYPE_WECHAT,
                com.clientservice.domain.entity.NotificationTemplate.PROVIDER_WECHAT))
                .thenReturn(Collections.emptyList());
        lenient().when(notificationRecordMapper.insert(any(NotificationRecord.class))).thenReturn(1);
    }

    @Nested
    @DisplayName("发送通知测试")
    class SendNotificationTests {

        @Test
        @DisplayName("发送通知应该调用所有启用的通知渠道")
        void sendNotificationAsync_ShouldCallAllEnabledChannels() throws Exception {
            // Given
            NotificationRecord smsRecord = NotificationRecord.builder()
                    .id(2L)
                    .status(NotificationRecord.STATUS_SUCCESS)
                    .build();
            NotificationRecord wechatRecord = NotificationRecord.builder()
                    .id(3L)
                    .status(NotificationRecord.STATUS_SUCCESS)
                    .build();

            when(emailNotificationService.sendEmail(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Email failed"));
            when(smsNotificationService.sendSms(anyString(), anyLong(), anyString(), anyLong(), anyString(), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    nullable(com.clientservice.domain.entity.NotificationTemplate.class)))
                    .thenReturn(smsRecord);
            when(wechatNotificationService.sendWechat(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(wechatRecord);

            // When
            notificationService.sendNotificationAsync(testMatter);

            // Then
            verify(emailNotificationService, times(1)).sendEmail(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString());
            verify(smsNotificationService, times(1)).sendSms(anyString(), anyLong(), anyString(), anyLong(), eq("13800138000"), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    nullable(com.clientservice.domain.entity.NotificationTemplate.class));
            verify(wechatNotificationService, times(1)).sendWechat(anyString(), anyLong(), anyString(), anyLong(), eq("test-openid"), nullable(String.class), anyString());
        }

        @Test
        @DisplayName("邮件通知失败不应该阻止其他通知渠道")
        void sendNotificationAsync_WithEmailFailure_ShouldContinueOtherChannels() throws Exception {
            // Given
            NotificationRecord smsRecord = NotificationRecord.builder()
                    .id(2L)
                    .status(NotificationRecord.STATUS_SUCCESS)
                    .build();

            lenient().when(emailNotificationService.sendEmail(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("邮件发送失败"));
            when(smsNotificationService.sendSms(anyString(), anyLong(), anyString(), anyLong(), anyString(), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    nullable(com.clientservice.domain.entity.NotificationTemplate.class)))
                    .thenReturn(smsRecord);

            // When - 不应该抛出异常
            assertDoesNotThrow(() -> notificationService.sendNotificationAsync(testMatter));

            // Then
            verify(emailNotificationService, times(1)).sendEmail(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString());
            verify(smsNotificationService, times(1)).sendSms(anyString(), anyLong(), anyString(), anyLong(), anyString(), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    nullable(com.clientservice.domain.entity.NotificationTemplate.class));
        }

        @Test
        @DisplayName("没有联系方式时不应该发送通知")
        void sendNotificationAsync_WithoutContactInfo_ShouldNotSend() throws Exception {
            // Given
            ContactInfoExtractResult notFound = ContactInfoExtractResult.builder()
                    .searchedFieldNames(new String[]{"phone"})
                    .build();
            when(contactResolver.extractPhoneNumber(anyMap())).thenReturn(notFound);
            when(contactResolver.extractEmail(anyMap())).thenReturn(ContactInfoExtractResult.builder()
                    .searchedFieldNames(new String[]{"email"})
                    .build());
            when(contactResolver.extractWechatOpenId(anyMap())).thenReturn(ContactInfoExtractResult.builder()
                    .searchedFieldNames(new String[]{"wechatOpenId"})
                    .build());

            // When
            notificationService.sendNotificationAsync(testMatter);

            // Then
            verify(emailNotificationService, never()).sendEmail(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString());
            verify(smsNotificationService, never()).sendSms(anyString(), anyLong(), anyString(), anyLong(), anyString(), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    any(com.clientservice.domain.entity.NotificationTemplate.class));
            verify(wechatNotificationService, never()).sendWechat(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("通知渠道未启用时不应该发送")
        void sendNotificationAsync_WithDisabledChannels_ShouldNotSend() throws Exception {
            // Given
            when(channelConfigService.isEmailEnabled()).thenReturn(false);
            when(channelConfigService.isSmsEnabled()).thenReturn(false);
            when(channelConfigService.isWechatEnabled()).thenReturn(false);

            // When
            notificationService.sendNotificationAsync(testMatter);

            // Then
            verify(emailNotificationService, never()).sendEmail(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString());
            verify(smsNotificationService, never()).sendSms(anyString(), anyLong(), anyString(), anyLong(), anyString(), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    any(com.clientservice.domain.entity.NotificationTemplate.class));
            verify(wechatNotificationService, never()).sendWechat(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("参数传递测试")
    class ContactExtractionTests {

        @Test
        @DisplayName("应该正确提取手机号")
        void extractPhoneNumber_ShouldExtractCorrectly() throws Exception {
            // When
            notificationService.sendNotificationAsync(testMatter);

            // Then
            verify(smsNotificationService, times(1)).sendSms(anyString(), any(), anyString(), any(), eq("13800138000"), 
                    any(com.clientservice.application.dto.NotificationTemplate.class), 
                    nullable(com.clientservice.domain.entity.NotificationTemplate.class));
        }

        @Test
        @DisplayName("应该正确提取邮箱")
        void extractEmail_ShouldExtractCorrectly() throws Exception {
            // When
            notificationService.sendNotificationAsync(testMatter);

            // Then
            verify(emailNotificationService, times(1)).sendEmail(anyString(), any(), anyString(), any(), eq("test@example.com"), anyString(), anyString());
        }

        @Test
        @DisplayName("应该正确提取微信OpenID")
        void extractWechatOpenId_ShouldExtractCorrectly() throws Exception {
            NotificationRecord wechatRecord = NotificationRecord.builder()
                    .id(3L)
                    .status(NotificationRecord.STATUS_SUCCESS)
                    .build();
            when(wechatNotificationService.sendWechat(anyString(), anyLong(), anyString(), anyLong(), anyString(), anyString(), anyString()))
                    .thenReturn(wechatRecord);

            // When
            notificationService.sendNotificationAsync(testMatter);

            // Then
            verify(wechatNotificationService, times(1)).sendWechat(anyString(), any(), anyString(), any(), eq("test-openid"), nullable(String.class), anyString());
        }
    }
}
