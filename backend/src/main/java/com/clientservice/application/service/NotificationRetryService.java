package com.clientservice.application.service;

import com.clientservice.common.util.JsonUtils;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通知重试服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationRetryService {

    private final NotificationRecordMapper notificationRecordMapper;
    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final WechatNotificationService wechatNotificationService;

    /** 默认最大重试次数 */
    @Value("${client-service.notification.retry.max-retries:3}")
    private int defaultMaxRetries;

    /** 重试间隔（分钟） */
    @Value("${client-service.notification.retry.interval-minutes:30}")
    private int retryIntervalMinutes;

    /**
     * 重试失败的通知
     * 查询所有失败且未达到最大重试次数的通知，进行重试
     */
    @Transactional
    public void retryFailedNotifications() {
        log.info("开始重试失败的通知...");

        try {
            // 查询需要重试的通知（失败状态、未达到最大重试次数、到了重试时间）
            LambdaQueryWrapper<NotificationRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(NotificationRecord::getStatus, NotificationRecord.STATUS_FAILED)
                    .le(NotificationRecord::getNextRetryAt, LocalDateTime.now())
                    .isNotNull(NotificationRecord::getNextRetryAt)
                    .eq(NotificationRecord::getDeleted, false);

            List<NotificationRecord> failedRecords = notificationRecordMapper.selectList(queryWrapper);
            
            // 过滤出未达到最大重试次数的记录
            failedRecords = failedRecords.stream()
                    .filter(record -> {
                        int maxRetries = record.getMaxRetries() != null ? record.getMaxRetries() : defaultMaxRetries;
                        int retryCount = record.getRetryCount() != null ? record.getRetryCount() : 0;
                        return retryCount < maxRetries;
                    })
                    .collect(Collectors.toList());

            if (failedRecords.isEmpty()) {
                log.debug("没有需要重试的通知");
                return;
            }

            log.info("发现{}条失败的通知需要重试", failedRecords.size());

            int successCount = 0;
            int failCount = 0;

            for (NotificationRecord record : failedRecords) {
                try {
                    // 更新重试次数和最后重试时间
                    record.setRetryCount((record.getRetryCount() == null ? 0 : record.getRetryCount()) + 1);
                    record.setLastRetryAt(LocalDateTime.now());

                    // 根据通知类型重试发送
                    boolean retrySuccess = retryNotification(record);

                    if (retrySuccess) {
                        record.setStatus(NotificationRecord.STATUS_SUCCESS);
                        record.setSentAt(LocalDateTime.now());
                        record.setNextRetryAt(null);
                        record.setErrorMessage(null);
                        successCount++;
                        log.info("通知重试成功: id={}, type={}, recipient={}, retryCount={}", 
                                record.getId(), record.getNotificationType(), record.getRecipient(), record.getRetryCount());
                    } else {
                        // 检查是否达到最大重试次数
                        int maxRetries = record.getMaxRetries() != null ? record.getMaxRetries() : defaultMaxRetries;
                        if (record.getRetryCount() >= maxRetries) {
                            record.setStatus(NotificationRecord.STATUS_FAILED);
                            record.setNextRetryAt(null);
                            record.setErrorMessage("已达到最大重试次数: " + maxRetries);
                            log.warn("通知重试失败且达到最大重试次数: id={}, type={}, recipient={}, retryCount={}", 
                                    record.getId(), record.getNotificationType(), record.getRecipient(), record.getRetryCount());
                        } else {
                            // 计算下次重试时间（指数退避：30分钟、1小时、2小时）
                            // retryCount已经被+1了（第76行），所以：
                            // retryCount=1时，延迟30*2^0=30分钟（第一次重试）
                            // retryCount=2时，延迟30*2^1=60分钟（第二次重试）
                            // retryCount=3时，延迟30*2^2=120分钟（第三次重试）
                            int delayMinutes = retryIntervalMinutes * (int) Math.pow(2, record.getRetryCount() - 1);
                            record.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
                            log.info("通知重试失败，将在{}分钟后重试: id={}, type={}, recipient={}, retryCount={}", 
                                    delayMinutes, record.getId(), record.getNotificationType(), record.getRecipient(), record.getRetryCount());
                        }
                        failCount++;
                    }

                    notificationRecordMapper.updateById(record);

                } catch (Exception e) {
                    log.error("重试通知时发生异常: id={}, type={}", record.getId(), record.getNotificationType(), e);
                    failCount++;
                }
            }

            log.info("通知重试完成: 成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("重试失败通知时发生异常", e);
        }
    }

    /**
     * 重试发送通知
     *
     * @param record 通知记录
     * @return 是否发送成功
     */
    private boolean retryNotification(final NotificationRecord record) {
        try {
            switch (record.getNotificationType()) {
                case NotificationRecord.TYPE_EMAIL:
                    // 解析邮件内容（subject和content）
                    String[] emailParts = parseEmailContent(record.getContent());
                    if (emailParts.length >= 2) {
                        return emailNotificationService.retrySendEmail(
                                record.getRecipient(),
                                emailParts[0], // subject
                                emailParts[1]  // content
                        );
                    }
                    return false;

                case NotificationRecord.TYPE_SMS:
                    return smsNotificationService.retrySendSms(
                            record.getRecipient(),
                            record.getContent()
                    );

                case NotificationRecord.TYPE_WECHAT:
                    // 解析微信模板数据
                    String[] wechatParts = parseWechatContent(record.getContent());
                    if (wechatParts.length >= 2) {
                        return wechatNotificationService.retrySendWechat(
                                record.getRecipient(),
                                wechatParts[0], // templateId
                                wechatParts[1]  // templateData
                        );
                    }
                    return false;

                default:
                    log.warn("未知的通知类型: {}", record.getNotificationType());
                    return false;
            }
        } catch (Exception e) {
            log.error("重试发送通知失败: id={}, type={}", record.getId(), record.getNotificationType(), e);
            return false;
        }
    }

    /**
     * 解析邮件内容（格式：JSON 或 subject|content）
     */
    private String[] parseEmailContent(final String content) {
        if (content == null || content.isEmpty()) {
            return new String[0];
        }
        
        // 尝试解析JSON格式
        if (content.trim().startsWith("{")) {
            try {
                Map<String, Object> map = JsonUtils.toMap(content);
                String subject = (String) map.get("subject");
                String body = (String) map.get("content");
                if (subject != null && body != null) {
                    return new String[]{subject, body};
                }
            } catch (Exception e) {
                log.warn("解析邮件内容JSON失败，尝试使用旧格式: {}", e.getMessage());
            }
        }
        
        // 旧格式兼容：subject|content
        int separatorIndex = content.indexOf("|");
        if (separatorIndex > 0) {
            return new String[]{
                    content.substring(0, separatorIndex),
                    content.substring(separatorIndex + 1)
            };
        }
        // 如果没有分隔符，假设整个内容是邮件正文
        return new String[]{"项目信息已更新", content};
    }

    /**
     * 解析微信内容（格式：templateId|templateData）
     */
    private String[] parseWechatContent(final String content) {
        if (content == null || content.isEmpty()) {
            return new String[0];
        }
        int separatorIndex = content.indexOf("|");
        if (separatorIndex > 0) {
            return new String[]{
                    content.substring(0, separatorIndex),
                    content.substring(separatorIndex + 1)
            };
        }
        return new String[0];
    }
}
