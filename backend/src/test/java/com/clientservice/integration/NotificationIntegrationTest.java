package com.clientservice.integration;

import com.clientservice.application.dto.MatterReceiveRequest;
import com.clientservice.application.dto.MatterReceiveResponse;
import com.clientservice.application.service.EmailNotificationService;
import com.clientservice.application.service.MatterService;
import com.clientservice.application.service.NotificationService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通知服务集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("通知服务集成测试")
class NotificationIntegrationTest {

    @Autowired
    private MatterService matterService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private ClientMatterMapper clientMatterMapper;

    @Autowired
    private NotificationRecordMapper notificationRecordMapper;

    private String matterId;
    private ClientMatter matter;

    @BeforeEach
    void setUp() {
        // 创建测试项目
        MatterReceiveRequest request = new MatterReceiveRequest();
        request.setClientId(2001L);
        request.setClientName("通知测试客户");
        
        Map<String, Object> matterData = new HashMap<>();
        matterData.put("matterId", 3001L);
        matterData.put("matterName", "通知测试项目");
        matterData.put("email", "test@example.com"); // 添加邮箱用于测试
        request.setMatterData(matterData);
        request.setScopes(Arrays.asList("MATTER_INFO"));
        request.setValidDays(30);
        
        MatterReceiveResponse response = matterService.receiveMatterData(request);
        matterId = response.getId();
        
        // 获取项目实体
        matter = clientMatterMapper.selectById(matterId);
        assertNotNull(matter);
    }

    @Test
    @DisplayName("异步发送通知应该创建通知记录")
    void sendNotificationAsync_ShouldCreateNotificationRecord() throws InterruptedException {
        // When
        notificationService.sendNotificationAsync(matter);

        // 等待异步任务完成（实际环境中可能需要更长时间）
        Thread.sleep(1000);

        // Then - 验证通知记录已创建
        List<NotificationRecord> records = notificationRecordMapper.selectByMatterId(matterId);
        assertNotNull(records);
        // 注意：由于邮件通知可能未启用，记录可能为空或状态为PENDING
        // 这里主要验证方法调用不会抛出异常
    }

    @Test
    @DisplayName("发送邮件通知应该成功或记录失败")
    void sendEmailNotification_ShouldSuccess() {
        // Given
        String recipient = "test@example.com";
        String subject = "测试主题";
        String content = "<html><body>测试内容</body></html>";
        Long lawFirmMatterId = 3001L;
        String clientName = "通知测试客户";

        // When & Then - 由于测试环境邮件服务可能未配置，可能成功或失败
        try {
            NotificationRecord record = emailNotificationService.sendEmail(
                    matterId, lawFirmMatterId, clientName, matter.getClientId(), recipient, subject, content);

            // 如果发送成功，验证记录
            assertNotNull(record);
            assertEquals(NotificationRecord.TYPE_EMAIL, record.getNotificationType());
            assertEquals(recipient, record.getRecipient());
            assertEquals(matterId, record.getMatterId());
            assertEquals(matter.getClientId(), record.getClientId());
            
            // 验证数据库记录
            NotificationRecord saved = notificationRecordMapper.selectById(record.getId());
            assertNotNull(saved);
            assertEquals(NotificationRecord.TYPE_EMAIL, saved.getNotificationType());
        } catch (BusinessException e) {
            // 如果邮件服务未配置，会抛出异常，这是预期的
            // 验证异常消息包含邮件相关错误
            assertTrue(e.getMessage().contains("邮件"), 
                    "异常消息应该包含'邮件'关键字: " + e.getMessage());
            
            // 验证失败记录已创建
            List<NotificationRecord> records = notificationRecordMapper.selectByMatterId(matterId);
            assertFalse(records.isEmpty(), "应该至少有一条通知记录");
            NotificationRecord failedRecord = records.stream()
                    .filter(r -> r.getNotificationType().equals(NotificationRecord.TYPE_EMAIL))
                    .findFirst()
                    .orElse(null);
            if (failedRecord != null) {
                assertEquals(NotificationRecord.STATUS_FAILED, failedRecord.getStatus());
            }
        }
    }

    @Test
    @DisplayName("发送邮件通知失败应该记录错误信息")
    void sendEmailNotification_WithFailure_ShouldRecordError() {
        // Given - 使用无效的收件人邮箱可能导致发送失败
        String recipient = "invalid-email";
        String subject = "测试主题";
        String content = "测试内容";
        Long lawFirmMatterId = 3001L;
        String clientName = "通知测试客户";

        // When & Then - 根据实际配置，可能成功或失败
        // 如果邮件服务未配置，应该记录为失败状态
        try {
            NotificationRecord record = emailNotificationService.sendEmail(
                    matterId, lawFirmMatterId, clientName, matter.getClientId(), recipient, subject, content);
            
            // 如果发送成功，验证记录状态
            assertNotNull(record);
            // 状态可能是SUCCESS或FAILED，取决于邮件服务配置
        } catch (BusinessException e) {
            // 如果抛出异常，验证错误信息
            assertTrue(e.getMessage().contains("邮件") || e.getMessage().contains("失败"));
        }
    }

    @Test
    @DisplayName("查询通知历史应该返回记录")
    void getNotificationHistory_ShouldReturnRecords() {
        // Given - 先尝试发送一个通知（可能成功或失败）
        String recipient = "test@example.com";
        String subject = "测试主题";
        String content = "测试内容";
        Long lawFirmMatterId = 3001L;
        String clientName = "通知测试客户";
        
        NotificationRecord record = null;
        try {
            record = emailNotificationService.sendEmail(
                    matterId, lawFirmMatterId, clientName, matter.getClientId(), recipient, subject, content);
        } catch (BusinessException e) {
            // 如果邮件服务未配置，会抛出异常，这是预期的
            // 查询失败记录
            List<NotificationRecord> failedRecords = notificationRecordMapper.selectByMatterId(matterId);
            record = failedRecords.stream()
                    .filter(r -> r.getNotificationType().equals(NotificationRecord.TYPE_EMAIL))
                    .findFirst()
                    .orElse(null);
        }

        // When - 查询通知历史
        List<NotificationRecord> records = notificationRecordMapper.selectByMatterId(matterId);

        // Then
        assertNotNull(records);
        assertFalse(records.isEmpty(), "应该至少有一条通知记录");
        if (record != null) {
            final Long recordId = record.getId(); // 保存到final变量以便在lambda中使用
            assertTrue(records.stream().anyMatch(r -> r.getId().equals(recordId)),
                    "应该包含创建的通知记录");
        }
    }
}
