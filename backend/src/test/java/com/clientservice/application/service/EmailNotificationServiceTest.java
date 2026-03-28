package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import com.clientservice.common.util.JsonUtils;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EmailNotificationService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailNotificationService 单元测试")
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @Mock
    private com.clientservice.infrastructure.config.MailConfig mailConfig;

    @Mock
    private com.clientservice.application.service.SysConfigService sysConfigService;

    @Mock
    private com.clientservice.application.service.CallbackService callbackService;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "fromEmailDefault", "noreply@example.com");
        ReflectionTestUtils.setField(emailNotificationService, "fromNameDefault", "客户服务系统");
        
        // Ensure mocks are valid
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Mock SysConfigService 返回默认值
        lenient().when(sysConfigService.getConfigValue(eq("client-service.notification.email.from"), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(sysConfigService.getConfigValue(eq("client-service.notification.email.from-name"), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        // Mock MailConfig 返回 mailSender
        lenient().when(mailConfig.createDynamicMailSender()).thenReturn(mailSender);
    }

    @Nested
    @DisplayName("发送邮件测试")
    class SendEmailTests {

        @Test
        @DisplayName("发送邮件应该成功")
        void sendEmail_ShouldSuccess() throws MessagingException {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String recipient = "test@example.com";
            String subject = "测试主题";
            String content = "<html><body>测试内容</body></html>";

            doAnswer(invocation -> {
                        // 模拟插入后设置ID
                        return 1;
                    }).when(notificationRecordMapper).insert(any(NotificationRecord.class));
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            // When
            NotificationRecord result = emailNotificationService.sendEmail(
                    matterId, lawFirmMatterId, clientName, clientId, recipient, subject, content);

            // Then
            assertNotNull(result);
            assertEquals(NotificationRecord.TYPE_EMAIL, result.getNotificationType());
            assertEquals(recipient, result.getRecipient());
            assertEquals(NotificationRecord.STATUS_SUCCESS, result.getStatus());
            assertNotNull(result.getSentAt());
            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(notificationRecordMapper, times(1)).insert(any(NotificationRecord.class));
            verify(notificationRecordMapper, times(1)).updateById(any(NotificationRecord.class));
        }

        @Test
        @DisplayName("邮件发送失败应该更新状态为失败")
        void sendEmail_WithSendFailure_ShouldUpdateStatusToFailed() throws MessagingException {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String recipient = "test@example.com";
            String subject = "测试主题";
            String content = "<html><body>测试内容</body></html>";

            doReturn(1).when(notificationRecordMapper).insert(any(NotificationRecord.class));
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            doThrow(new RuntimeException("邮件发送失败")).when(mailSender).send(any(MimeMessage.class));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> emailNotificationService.sendEmail(matterId, lawFirmMatterId, clientName, clientId, recipient, subject, content));
            
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getCode());
            assertTrue(exception.getMessage().contains("邮件发送失败"));
            
            // 验证状态更新为失败
            verify(notificationRecordMapper, times(1)).updateById(argThat(record -> 
                    NotificationRecord.STATUS_FAILED.equals(((NotificationRecord) record).getStatus())));
        }

        @Test
        @DisplayName("MessagingException应该被捕获并转换为BusinessException")
        void sendEmail_WithMessagingException_ShouldThrowBusinessException() throws MessagingException {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String recipient = "test@example.com";
            String subject = "测试主题";
            String content = "<html><body>测试内容</body></html>";

            doReturn(1).when(notificationRecordMapper).insert(any(NotificationRecord.class));
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            // MessagingException是checked exception，需要使用doAnswer包装
            doAnswer(invocation -> {
                throw new MessagingException("邮件服务异常");
            }).when(mailSender).send(any(MimeMessage.class));

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, 
                    () -> emailNotificationService.sendEmail(matterId, lawFirmMatterId, clientName, clientId, recipient, subject, content));
            
            assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getCode());
            assertTrue(exception.getMessage().contains("邮件发送失败"));
        }
    }

    @Nested
    @DisplayName("通知记录测试")
    class NotificationRecordTests {

        @Test
        @DisplayName("创建的通知记录应该包含正确的字段")
        void createNotificationRecord_ShouldHaveCorrectFields() throws MessagingException {
            // Given
            String matterId = "CS1234567890123456789";
            Long lawFirmMatterId = 1001L;
            String clientName = "测试客户";
            Long clientId = 2001L;
            String recipient = "test@example.com";
            String subject = "测试主题";
            String content = "测试内容";

            doAnswer(invocation -> {
                NotificationRecord record = invocation.getArgument(0);
                assertNotNull(record);
                assertEquals(matterId, record.getMatterId());
                assertEquals(clientId, record.getClientId());
                assertEquals(NotificationRecord.TYPE_EMAIL, record.getNotificationType());
                assertEquals(recipient, record.getRecipient());
                assertEquals(NotificationRecord.STATUS_PENDING, record.getStatus());
                
                // 验证内容是否为JSON格式
                Map<String, Object> contentMap = JsonUtils.toMap(record.getContent());
                assertEquals(subject, contentMap.get("subject"));
                assertEquals(content, contentMap.get("content"));
                
                return 1;
            }).when(notificationRecordMapper).insert(any(NotificationRecord.class));
            when(notificationRecordMapper.updateById(any(NotificationRecord.class))).thenReturn(1);
            doNothing().when(mailSender).send(any(MimeMessage.class));

            // When
            emailNotificationService.sendEmail(matterId, lawFirmMatterId, clientName, clientId, recipient, subject, content);

            // Then
            verify(notificationRecordMapper, times(1)).insert(any(NotificationRecord.class));
        }
    }
}
