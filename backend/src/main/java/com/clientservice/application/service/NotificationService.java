package com.clientservice.application.service;

import com.clientservice.application.dto.ContactInfoExtractResult;
import com.clientservice.application.dto.NotificationTemplate;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailNotificationService emailNotificationService;
    private final SmsNotificationService smsNotificationService;
    private final WechatNotificationService wechatNotificationService;
    private final NotificationChannelConfigService channelConfigService;
    private final NotificationContactResolver contactResolver;
    private final NotificationContentService notificationContentService;
    private final NotificationTemplateMapper templateMapper;
    private final NotificationRecordMapper notificationRecordMapper;

    /**
     * 异步发送通知
     *
     * @param matter 项目数据
     */
    @Async
    public void sendNotificationAsync(final ClientMatter matter) {
        try {
            log.info("开始发送通知: matterId={}, clientId={}", matter.getId(), matter.getClientId());

            Map<String, Object> matterData = contactResolver.parseMatterData(matter.getMatterData());
            String fieldNames = matterData.keySet().stream()
                    .collect(Collectors.joining(", "));
            log.info("项目数据字段名: [{}]", fieldNames);

            NotificationTemplate template = notificationContentService.buildNotificationTemplate(matter);

            ContactInfoExtractResult phoneResult = contactResolver.extractPhoneNumber(matterData);
            ContactInfoExtractResult emailResult = contactResolver.extractEmail(matterData);
            ContactInfoExtractResult wechatResult = contactResolver.extractWechatOpenId(matterData);

            processEmailChannel(matter, template, emailResult);
            processSmsChannel(matter, template, phoneResult);
            processWechatChannel(matter, template, wechatResult);

            log.info("通知发送完成: matterId={}", matter.getId());
        } catch (Exception e) {
            log.error("发送通知失败: matterId={}", matter.getId(), e);
        }
    }

    private void processEmailChannel(
            final ClientMatter matter,
            final NotificationTemplate template,
            final ContactInfoExtractResult emailResult) {
        if (!channelConfigService.isEmailEnabled()) {
            recordChannelDisabled(matter, NotificationRecord.TYPE_EMAIL, emailResult.getValue());
            return;
        }
        if (!emailResult.isFound()) {
            recordMissingContact(matter, NotificationRecord.TYPE_EMAIL, emailResult, "邮箱");
            return;
        }
        try {
            sendEmailNotification(matter, template, emailResult.getValue());
        } catch (Exception e) {
            log.error("邮件通知发送失败: matterId={}", matter.getId(), e);
        }
    }

    private void processSmsChannel(
            final ClientMatter matter,
            final NotificationTemplate template,
            final ContactInfoExtractResult phoneResult) {
        if (!channelConfigService.isSmsEnabled()) {
            recordChannelDisabled(matter, NotificationRecord.TYPE_SMS, phoneResult.getValue());
            return;
        }
        if (!phoneResult.isFound()) {
            recordMissingContact(matter, NotificationRecord.TYPE_SMS, phoneResult, "手机号");
            return;
        }
        try {
            sendSmsNotification(matter, template, phoneResult.getValue());
        } catch (Exception e) {
            log.error("短信通知发送失败: matterId={}", matter.getId(), e);
        }
    }

    private void processWechatChannel(
            final ClientMatter matter,
            final NotificationTemplate template,
            final ContactInfoExtractResult wechatResult) {
        if (!channelConfigService.isWechatEnabled()) {
            recordChannelDisabled(matter, NotificationRecord.TYPE_WECHAT, wechatResult.getValue());
            return;
        }
        if (!wechatResult.isFound()) {
            recordMissingContact(matter, NotificationRecord.TYPE_WECHAT, wechatResult, "微信OpenID");
            return;
        }
        try {
            sendWechatNotification(matter, template, wechatResult.getValue());
        } catch (Exception e) {
            log.error("微信通知发送失败: matterId={}", matter.getId(), e);
        }
    }

    private void recordMissingContact(
            final ClientMatter matter,
            final String notificationType,
            final ContactInfoExtractResult result,
            final String logLabel) {
        createFailureRecord(
                matter,
                notificationType,
                result.getValue(),
                result.getSearchedFieldNames(),
                NotificationRecord.ERROR_DATA_NOT_FOUND);
        log.warn("{}提取失败: matterId={}, 已查找字段: {}",
                logLabel, matter.getId(), String.join(", ", result.getSearchedFieldNames()));
    }

    private void recordChannelDisabled(
            final ClientMatter matter,
            final String notificationType,
            final String recipient) {
        createFailureRecord(
                matter,
                notificationType,
                recipient,
                new String[0],
                NotificationRecord.ERROR_CHANNEL_DISABLED);
        log.info("{}渠道未启用，跳过发送: matterId={}", notificationType, matter.getId());
    }

    private void createFailureRecord(final ClientMatter matter, final String notificationType,
            final String recipient, final String[] searchedFieldNames, final String errorCode) {
        String errorMessage;
        if (NotificationRecord.ERROR_CHANNEL_DISABLED.equals(errorCode)) {
            errorMessage = "通知渠道未启用";
        } else {
            errorMessage = String.format("未找到联系方式字段，已查找字段: %s",
                    String.join(", ", searchedFieldNames));
        }

        NotificationRecord record = NotificationRecord.builder()
                .matterId(matter.getId())
                .clientId(matter.getClientId())
                .lawFirmMatterId(matter.getLawFirmMatterId())
                .clientName(matter.getClientName())
                .notificationType(notificationType)
                .recipient(recipient != null ? recipient : "")
                .status(NotificationRecord.STATUS_FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();

        notificationRecordMapper.insert(record);
    }

    private void sendEmailNotification(
            final ClientMatter matter,
            final NotificationTemplate template,
            final String email) {

        // 从数据库获取启用的邮件模板
        List<com.clientservice.domain.entity.NotificationTemplate> templates =
                templateMapper.selectEnabledByTypeAndProvider(
                        com.clientservice.domain.entity.NotificationTemplate.TYPE_EMAIL, null);

        com.clientservice.domain.entity.NotificationTemplate emailTemplate =
                templates.isEmpty() ? null : templates.get(0);

        String subject = String.format("【%s】项目信息已更新", template.getMatterName());
        String content = notificationContentService.buildEmailContent(template, emailTemplate);

        emailNotificationService.sendEmail(
                matter.getId(),
                matter.getLawFirmMatterId(),
                matter.getClientName(),
                matter.getClientId(),
                email,
                subject,
                content);
    }

    private void sendSmsNotification(
            final ClientMatter matter,
            final NotificationTemplate template,
            final String phoneNumber) {

        String provider = channelConfigService.getSmsProvider();

        List<com.clientservice.domain.entity.NotificationTemplate> templates =
                templateMapper.selectEnabledByTypeAndProvider(
                        com.clientservice.domain.entity.NotificationTemplate.TYPE_SMS, provider);

        com.clientservice.domain.entity.NotificationTemplate smsTemplate =
                templates.isEmpty() ? null : templates.get(0);

        // 调用短信服务，传入模板对象（包含 templateContent）
        smsNotificationService.sendSms(
                matter.getId(),
                matter.getLawFirmMatterId(),
                matter.getClientName(),
                matter.getClientId(),
                phoneNumber,
                template,
                smsTemplate);
    }

    private void sendWechatNotification(
            final ClientMatter matter,
            final NotificationTemplate template,
            final String openId) {

        // 从数据库获取启用的微信模板
        List<com.clientservice.domain.entity.NotificationTemplate> templates =
                templateMapper.selectEnabledByTypeAndProvider(
                        com.clientservice.domain.entity.NotificationTemplate.TYPE_WECHAT,
                        com.clientservice.domain.entity.NotificationTemplate.PROVIDER_WECHAT);

        String templateId = "";
        String templateContent = null;

        if (!templates.isEmpty()) {
            com.clientservice.domain.entity.NotificationTemplate wechatTemplate = templates.get(0);
            templateId = wechatTemplate.getTemplateCode() != null ? wechatTemplate.getTemplateCode() : "";
            templateContent = wechatTemplate.getTemplateContent();
        } else {
            templateId = channelConfigService.getWechatTemplateId();
        }

        String templateData = notificationContentService.buildWechatTemplateData(templateContent, template);

        wechatNotificationService.sendWechat(
                matter.getId(),
                matter.getLawFirmMatterId(),
                matter.getClientName(),
                matter.getClientId(),
                openId,
                templateId,
                templateData);
    }
}
