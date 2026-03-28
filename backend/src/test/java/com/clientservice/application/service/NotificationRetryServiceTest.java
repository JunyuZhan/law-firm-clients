package com.clientservice.application.service;

import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRetryServiceTest {

    @Mock
    private NotificationRecordMapper notificationRecordMapper;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private SmsNotificationService smsNotificationService;

    @Mock
    private WechatNotificationService wechatNotificationService;

    @InjectMocks
    private NotificationRetryService notificationRetryService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificationRetryService, "defaultMaxRetries", 3);
        ReflectionTestUtils.setField(notificationRetryService, "retryIntervalMinutes", 30);
    }

    @Test
    @DisplayName("没有需要重试的通知")
    void retryFailedNotifications_NoRecords() {
        // Given
        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.emptyList());

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        verify(notificationRecordMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("重试邮件通知成功")
    void retryFailedNotifications_EmailSuccess() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(1L);
        record.setNotificationType(NotificationRecord.TYPE_EMAIL);
        record.setRecipient("test@example.com");
        record.setContent("Subject|Content");
        record.setRetryCount(0);
        record.setMaxRetries(3);
        record.setStatus(NotificationRecord.STATUS_FAILED);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(emailNotificationService.retrySendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        verify(emailNotificationService).retrySendEmail("test@example.com", "Subject", "Content");
        
        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRecordMapper).updateById(recordCaptor.capture());
        
        NotificationRecord updatedRecord = recordCaptor.getValue();
        assertEquals(NotificationRecord.STATUS_SUCCESS, updatedRecord.getStatus());
        assertEquals(1, updatedRecord.getRetryCount());
        assertNotNull(updatedRecord.getLastRetryAt());
        assertNotNull(updatedRecord.getSentAt());
        assertNull(updatedRecord.getNextRetryAt());
    }

    @Test
    @DisplayName("重试短信通知成功")
    void retryFailedNotifications_SmsSuccess() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(2L);
        record.setNotificationType(NotificationRecord.TYPE_SMS);
        record.setRecipient("13800138000");
        record.setContent("SMS Content");
        record.setRetryCount(0);
        record.setMaxRetries(3);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(smsNotificationService.retrySendSms(anyString(), anyString())).thenReturn(true);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        verify(smsNotificationService).retrySendSms("13800138000", "SMS Content");
        
        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRecordMapper).updateById(recordCaptor.capture());
        
        NotificationRecord updatedRecord = recordCaptor.getValue();
        assertEquals(NotificationRecord.STATUS_SUCCESS, updatedRecord.getStatus());
    }

    @Test
    @DisplayName("重试微信通知成功")
    void retryFailedNotifications_WechatSuccess() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(3L);
        record.setNotificationType(NotificationRecord.TYPE_WECHAT);
        record.setRecipient("openid123");
        record.setContent("template123|{\"key\":\"value\"}");
        record.setRetryCount(0);
        record.setMaxRetries(3);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(wechatNotificationService.retrySendWechat(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        verify(wechatNotificationService).retrySendWechat("openid123", "template123", "{\"key\":\"value\"}");
        
        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRecordMapper).updateById(recordCaptor.capture());
        
        NotificationRecord updatedRecord = recordCaptor.getValue();
        assertEquals(NotificationRecord.STATUS_SUCCESS, updatedRecord.getStatus());
    }

    @Test
    @DisplayName("重试邮件通知成功（JSON格式）")
    void retryFailedNotifications_EmailJsonSuccess() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(10L);
        record.setNotificationType(NotificationRecord.TYPE_EMAIL);
        record.setRecipient("test@example.com");
        record.setContent("{\"subject\":\"JSON Subject\",\"content\":\"JSON Content\"}");
        record.setRetryCount(0);
        record.setMaxRetries(3);
        record.setStatus(NotificationRecord.STATUS_FAILED);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(emailNotificationService.retrySendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        verify(emailNotificationService).retrySendEmail("test@example.com", "JSON Subject", "JSON Content");
        
        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRecordMapper).updateById(recordCaptor.capture());
        
        NotificationRecord updatedRecord = recordCaptor.getValue();
        assertEquals(NotificationRecord.STATUS_SUCCESS, updatedRecord.getStatus());
    }

    @Test
    @DisplayName("重试失败但未达到最大重试次数")
    void retryFailedNotifications_FailRetryAgain() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(4L);
        record.setNotificationType(NotificationRecord.TYPE_EMAIL);
        record.setRecipient("test@example.com");
        record.setContent("Subject|Content");
        record.setRetryCount(0);
        record.setMaxRetries(3);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(emailNotificationService.retrySendEmail(anyString(), anyString(), anyString())).thenReturn(false);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRecordMapper).updateById(recordCaptor.capture());
        
        NotificationRecord updatedRecord = recordCaptor.getValue();
        // retryCount should be incremented to 1
        assertEquals(1, updatedRecord.getRetryCount());
        // Status should remain whatever it was (or technically still failed, but method doesn't explicitly set FAILED unless max retries reached, 
        // but it does set nextRetryAt)
        // Check next retry time
        assertNotNull(updatedRecord.getNextRetryAt());
        // Delay for 1st retry (retryCount 1) is 30 * 2^(1-1) = 30 mins
        // We can't easily assert exact time, but we can check it's in future
        assertTrue(updatedRecord.getNextRetryAt().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("重试失败且达到最大重试次数")
    void retryFailedNotifications_FailMaxRetries() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(5L);
        record.setNotificationType(NotificationRecord.TYPE_EMAIL);
        record.setRecipient("test@example.com");
        record.setContent("Subject|Content");
        record.setRetryCount(2); // This will become 3 after increment
        record.setMaxRetries(3);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(emailNotificationService.retrySendEmail(anyString(), anyString(), anyString())).thenReturn(false);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        ArgumentCaptor<NotificationRecord> recordCaptor = ArgumentCaptor.forClass(NotificationRecord.class);
        verify(notificationRecordMapper).updateById(recordCaptor.capture());
        
        NotificationRecord updatedRecord = recordCaptor.getValue();
        assertEquals(3, updatedRecord.getRetryCount());
        assertEquals(NotificationRecord.STATUS_FAILED, updatedRecord.getStatus());
        assertNull(updatedRecord.getNextRetryAt());
        assertNotNull(updatedRecord.getErrorMessage());
    }
    
    @Test
    @DisplayName("邮件内容解析失败")
    void retryFailedNotifications_EmailParseFail() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(6L);
        record.setNotificationType(NotificationRecord.TYPE_EMAIL);
        record.setContent(""); // Empty content
        
        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        
        // When
        notificationRetryService.retryFailedNotifications();
        
        // Then
        verify(emailNotificationService, never()).retrySendEmail(anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("微信内容解析失败")
    void retryFailedNotifications_WechatParseFail() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(7L);
        record.setNotificationType(NotificationRecord.TYPE_WECHAT);
        record.setContent("InvalidFormat"); // No separator
        
        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        
        // When
        notificationRetryService.retryFailedNotifications();
        
        // Then
        verify(wechatNotificationService, never()).retrySendWechat(anyString(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("邮件默认主题解析")
    void retryFailedNotifications_EmailDefaultSubject() {
        // Given
        NotificationRecord record = new NotificationRecord();
        record.setId(8L);
        record.setNotificationType(NotificationRecord.TYPE_EMAIL);
        record.setRecipient("test@example.com");
        record.setContent("Just Content No Separator");
        record.setRetryCount(0);
        record.setMaxRetries(3);

        when(notificationRecordMapper.selectList(any())).thenReturn(Collections.singletonList(record));
        when(emailNotificationService.retrySendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationRetryService.retryFailedNotifications();

        // Then
        verify(emailNotificationService).retrySendEmail("test@example.com", "项目信息已更新", "Just Content No Separator");
    }
}
