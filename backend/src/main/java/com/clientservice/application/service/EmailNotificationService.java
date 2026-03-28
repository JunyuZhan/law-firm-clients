package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.common.util.JsonUtils;
import com.clientservice.infrastructure.config.MailConfig;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮件通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;
    private final NotificationRecordMapper notificationRecordMapper;
    private final SysConfigService sysConfigService;
    private final CallbackService callbackService;

    @Value("${client-service.notification.email.from:noreply@example.com}")
    private String fromEmailDefault;

    @Value("${client-service.notification.email.from-name:客户服务系统}")
    private String fromNameDefault;

    /**
     * 获取发件人邮箱（优先从数据库读取，如果数据库没有则使用配置文件默认值）
     */
    private String getFromEmail() {
        return sysConfigService.getConfigValue("client-service.notification.email.from", fromEmailDefault);
    }

    /**
     * 获取发件人名称（优先从数据库读取，如果数据库没有则使用配置文件默认值）
     */
    private String getFromName() {
        return sysConfigService.getConfigValue("client-service.notification.email.from-name", fromNameDefault);
    }

    /**
     * 获取邮件发送器（动态从数据库读取配置）
     * 每次发送邮件时都会重新创建，确保使用最新的配置
     */
    private JavaMailSender getMailSender() {
        // 优先使用动态创建的邮件发送器（从数据库读取最新配置）
        // 如果数据库没有配置，则使用启动时创建的 Bean
        try {
            JavaMailSender dynamicSender = mailConfig.createDynamicMailSender();
            if (dynamicSender != null) {
                return dynamicSender;
            }
        } catch (Exception e) {
            log.warn("动态创建邮件发送器失败，使用默认配置: {}", e.getMessage());
        }
        return mailSender; // 回退到默认的 Bean
    }

    @Value("${client-service.notification.retry.max-retries:3}")
    private int defaultMaxRetries;

    @Value("${client-service.notification.retry.interval-minutes:30}")
    private int retryIntervalMinutes;

    /**
     * 发送邮件通知
     *
     * @param matterId 项目ID
     * @param lawFirmMatterId 律所系统项目ID
     * @param clientName 客户名称
     * @param clientId 客户ID
     * @param recipient 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（HTML格式）
     * @return 通知记录
     */
    @Transactional
    public NotificationRecord sendEmail(
            final String matterId,
            final Long lawFirmMatterId,
            final String clientName,
            final Long clientId,
            final String recipient,
            final String subject,
            final String content) {

        // 创建通知记录（待发送状态）
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("subject", subject);
        contentMap.put("content", content);
        
        NotificationRecord record = NotificationRecord.builder()
                .matterId(matterId)
                .clientId(clientId)
                .lawFirmMatterId(lawFirmMatterId)
                .clientName(clientName)
                .notificationType(NotificationRecord.TYPE_EMAIL)
                .recipient(recipient)
                .content(JsonUtils.toJson(contentMap)) // 使用JSON存储subject和content
                .status(NotificationRecord.STATUS_PENDING)
                .retryCount(0)
                .maxRetries(defaultMaxRetries)
                .build();

        notificationRecordMapper.insert(record);

        try {
            // 动态获取邮件发送器（支持配置更新后自动使用新配置）
            JavaMailSender currentMailSender = getMailSender();
            
            // 邮件注入防护：移除换行符并验证收件人格式
            String safeRecipient = recipient.replaceAll("[\\r\\n]", "").trim();
            if (!safeRecipient.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
                    || safeRecipient.length() > 254) {
                throw new MessagingException("收件人邮箱格式无效: " + safeRecipient);
            }
            // 清理主题中的换行符（防止邮件头注入）
            String safeSubject = subject != null ? subject.replaceAll("[\\r\\n]", " ").trim() : "";

            // 创建邮件消息
            MimeMessage message = currentMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromEmail(), getFromName());
            helper.setTo(safeRecipient);
            helper.setSubject(safeSubject);
            helper.setText(content, true); // true表示HTML格式

            // 发送邮件
            currentMailSender.send(message);

            // 更新通知记录为成功
            record.setStatus(NotificationRecord.STATUS_SUCCESS);
            record.setSentAt(LocalDateTime.now());
            notificationRecordMapper.updateById(record);

            log.info("邮件发送成功: matterId={}, recipient={}", matterId, safeRecipient);

            // 回调通知发送成功
            callbackService.callbackNotificationSuccess(record);

        } catch (MessagingException e) {
            log.error("邮件发送失败: matterId={}, recipient={}", matterId, recipient, e);
            handleNotificationFailure(record, e.getMessage());
            // 回调通知发送失败
            callbackService.callbackNotificationFailure(record);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "邮件发送失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("邮件发送异常: matterId={}, recipient={}", matterId, recipient, e);
            handleNotificationFailure(record, e.getMessage());
            // 回调通知发送失败
            callbackService.callbackNotificationFailure(record);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "邮件发送异常: " + e.getMessage());
        }

        return record;
    }

    /**
     * 发送系统告警邮件
     *
     * @param recipient 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Transactional
    public void sendSystemAlert(String recipient, String subject, String content) {
        if (recipient == null || recipient.isEmpty()) {
            log.warn("系统告警邮件发送失败：收件人为空");
            return;
        }

        // 创建通知记录（系统告警）
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("subject", subject);
        contentMap.put("content", content);

        NotificationRecord record = NotificationRecord.builder()
                .matterId("SYSTEM_ALERT") // 特殊标识
                .notificationType(NotificationRecord.TYPE_EMAIL)
                .recipient(recipient)
                .content(JsonUtils.toJson(contentMap))
                .status(NotificationRecord.STATUS_PENDING)
                .retryCount(0)
                .maxRetries(defaultMaxRetries)
                .build();

        notificationRecordMapper.insert(record);

        try {
            boolean success = retrySendEmail(recipient, subject, content);
            
            if (success) {
                record.setStatus(NotificationRecord.STATUS_SUCCESS);
                record.setSentAt(LocalDateTime.now());
                notificationRecordMapper.updateById(record);
                log.info("系统告警邮件发送成功: recipient={}", recipient);
            } else {
                handleNotificationFailure(record, "发送失败");
                log.error("系统告警邮件发送失败: recipient={}", recipient);
            }
            
        } catch (Exception e) {
            log.error("系统告警邮件发送异常: recipient={}", recipient, e);
            handleNotificationFailure(record, e.getMessage());
        }
    }

    /**
     * 处理通知发送失败，设置重试信息
     *
     * @param record 通知记录
     * @param errorMessage 错误信息
     */
    private void handleNotificationFailure(final NotificationRecord record, final String errorMessage) {
        record.setStatus(NotificationRecord.STATUS_FAILED);
        record.setErrorMessage(errorMessage);
        
        // 设置重试信息
        if (record.getRetryCount() == null) {
            record.setRetryCount(0);
        }
        if (record.getMaxRetries() == null) {
            record.setMaxRetries(defaultMaxRetries);
        }
        
        // 如果未达到最大重试次数，设置下次重试时间
        if (record.getRetryCount() < record.getMaxRetries()) {
            int delayMinutes = retryIntervalMinutes * (int) Math.pow(2, record.getRetryCount());
            record.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
        }
        
        notificationRecordMapper.updateById(record);
    }

    /**
     * 重试发送邮件（不创建新记录，用于重试）
     *
     * @param recipient 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容（HTML格式）
     * @return 是否发送成功
     */
    public boolean retrySendEmail(final String recipient, final String subject, final String content) {
        try {
            // 动态获取邮件发送器（支持配置更新后自动使用新配置）
            JavaMailSender currentMailSender = getMailSender();
            
            // 创建邮件消息
            MimeMessage message = currentMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(getFromEmail(), getFromName());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式

            // 发送邮件
            currentMailSender.send(message);

            log.info("邮件重试发送成功: recipient={}", recipient);
            return true;

        } catch (Exception e) {
            log.error("邮件重试发送失败: recipient={}", recipient, e);
            return false;
        }
    }
}
