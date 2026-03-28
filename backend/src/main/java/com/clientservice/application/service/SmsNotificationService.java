package com.clientservice.application.service;

import com.clientservice.application.util.TemplateVariableReplacer;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.domain.entity.NotificationTemplate;
import com.clientservice.infrastructure.notification.aliyun.AliyunSmsClient;
import com.clientservice.infrastructure.notification.tencent.TencentSmsClient;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 短信通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotificationService {

    private final NotificationRecordMapper notificationRecordMapper;
    private final SysConfigService sysConfigService;
    private final TencentSmsClient tencentSmsClient;
    private final AliyunSmsClient aliyunSmsClient;
    private final CallbackService callbackService;
    private final NotificationTemplateMapper templateMapper;
    private final ObjectMapper objectMapper;

    @Value("${client-service.notification.sms.enabled:false}")
    private boolean smsEnabledDefault;

    @Value("${client-service.notification.sms.provider:aliyun}")
    private String providerDefault;

    /**
     * 获取短信通知是否启用（优先从数据库读取，如果数据库没有则使用配置文件默认值）
     */
    private boolean isSmsEnabled() {
        // 兼容两种键名格式：带前缀和不带前缀
        String value = sysConfigService.getConfigValue("client-service.notification.sms.enabled");
        if (value == null) {
            value = sysConfigService.getConfigValue("notification.sms.enabled");
        }
        return value != null ? Boolean.parseBoolean(value) : smsEnabledDefault;
    }

    /**
     * 获取短信服务商（优先从数据库读取，如果数据库没有则使用配置文件默认值）
     */
    private String getProvider() {
        // 兼容两种键名格式：带前缀和不带前缀
        String value = sysConfigService.getConfigValue("client-service.notification.sms.provider");
        if (value == null) {
            value = sysConfigService.getConfigValue("notification.sms.provider");
        }
        return value != null ? value : providerDefault;
    }

    @Value("${client-service.notification.retry.max-retries:3}")
    private int defaultMaxRetries;

    @Value("${client-service.notification.retry.interval-minutes:30}")
    private int retryIntervalMinutes;

    /**
     * 发送短信通知
     *
     * @param matterId 项目ID
     * @param lawFirmMatterId 律所系统项目ID
     * @param clientName 客户名称
     * @param clientId 客户ID
     * @param phoneNumber 手机号
     * @param template 通知模板（包含变量数据）
     * @param smsTemplate 短信模板（数据库模板，包含 templateContent）
     * @return 通知记录
     */
    @Transactional
    public NotificationRecord sendSms(
            final String matterId,
            final Long lawFirmMatterId,
            final String clientName,
            final Long clientId,
            final String phoneNumber,
            final com.clientservice.application.dto.NotificationTemplate template,
            final NotificationTemplate smsTemplate) {

        // 检查短信服务是否启用
        if (!isSmsEnabled()) {
            log.info("短信通知未启用，跳过发送: phoneNumber={}", phoneNumber);
            return null;
        }

        // 构建短信内容（用于记录）
        String content = buildSmsContent(template, smsTemplate);

        // 创建通知记录（待发送状态）
        NotificationRecord record = NotificationRecord.builder()
                .matterId(matterId)
                .clientId(clientId)
                .lawFirmMatterId(lawFirmMatterId)
                .clientName(clientName)
                .notificationType(NotificationRecord.TYPE_SMS)
                .recipient(phoneNumber)
                .content(content)
                .status(NotificationRecord.STATUS_PENDING)
                .retryCount(0)
                .maxRetries(defaultMaxRetries)
                .build();

        notificationRecordMapper.insert(record);

        try {
            // 获取服务商
            String provider = getProvider();
            
            // 根据配置的提供商发送短信
            boolean sendSuccess = sendSmsByProvider(provider, phoneNumber, template, smsTemplate, matterId);

            if (sendSuccess) {
                record.setStatus(NotificationRecord.STATUS_SUCCESS);
                record.setSentAt(LocalDateTime.now());
                notificationRecordMapper.updateById(record);
                log.info("短信发送成功: matterId={}, phoneNumber={}, provider={}",
                        matterId, phoneNumber, provider);
                // 回调通知发送成功
                callbackService.callbackNotificationSuccess(record);
            } else {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "短信发送失败");
            }

        } catch (Exception e) {
            log.error("短信发送失败: matterId={}, phoneNumber={}", matterId, phoneNumber, e);
            handleNotificationFailure(record, e.getMessage());
            // 回调通知发送失败
            callbackService.callbackNotificationFailure(record);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "短信发送失败: " + e.getMessage());
        }

        return record;
    }

    /**
     * 根据提供商发送短信（通用方法）
     *
     * @param provider 短信提供商（aliyun/tencent）
     * @param phoneNumber 手机号
     * @param template 通知模板（包含变量数据）
     * @param smsTemplate 短信模板（数据库模板，包含 templateContent）
     * @param matterId 项目ID（可选，用于日志记录）
     * @return 是否发送成功
     */
    private boolean sendSmsByProvider(
            final String provider,
            final String phoneNumber,
            final com.clientservice.application.dto.NotificationTemplate template,
            final NotificationTemplate smsTemplate,
            final String matterId) {
        
        if ("aliyun".equalsIgnoreCase(provider)) {
            if (aliyunSmsClient.isConfigured()) {
                return sendViaAliyun(phoneNumber, template, smsTemplate);
            } else {
                String logContext = matterId != null 
                        ? String.format("matterId=%s, phoneNumber=%s", matterId, phoneNumber)
                        : String.format("phoneNumber=%s", phoneNumber);
                log.warn("阿里云短信未配置，使用模拟发送: {}", logContext);
                return true; // 模拟发送成功
            }
        } else if ("tencent".equalsIgnoreCase(provider)) {
            if (tencentSmsClient.isConfigured()) {
                return sendViaTencent(phoneNumber, template, smsTemplate);
            } else {
                String logContext = matterId != null 
                        ? String.format("matterId=%s, phoneNumber=%s", matterId, phoneNumber)
                        : String.format("phoneNumber=%s", phoneNumber);
                log.warn("腾讯云短信未配置，使用模拟发送: {}", logContext);
                return true; // 模拟发送成功
            }
        } else {
            String logContext = matterId != null 
                    ? String.format("provider=%s, matterId=%s, phoneNumber=%s", provider, matterId, phoneNumber)
                    : String.format("provider=%s, phoneNumber=%s", provider, phoneNumber);
            log.warn("未知的短信提供商: {}, 使用模拟发送: {}", provider, logContext);
            return true; // 模拟发送成功
        }
    }

    /**
     * 通过阿里云发送短信
     *
     * @param phoneNumber 手机号
     * @param template 通知模板（包含变量数据）
     * @param smsTemplate 短信模板（数据库模板，包含 templateContent）
     * @return 是否发送成功
     */
    private boolean sendViaAliyun(
            final String phoneNumber,
            final com.clientservice.application.dto.NotificationTemplate template,
            final NotificationTemplate smsTemplate) {
        try {
            String templateParamJson;
            
            // 如果数据库中有模板内容，使用模板内容构建参数
            if (smsTemplate != null && smsTemplate.getTemplateContent() != null 
                    && !smsTemplate.getTemplateContent().isEmpty()) {
                // 从模板内容构建参数（JSON格式，变量名需与阿里云平台一致）
                templateParamJson = buildAliyunTemplateParams(smsTemplate.getTemplateContent(), template);
            } else {
                // 兼容旧代码：如果没有模板内容，使用默认格式
                Map<String, String> templateParam = new HashMap<>();
                templateParam.put("content", TemplateVariableReplacer.buildDefaultSmsContent(template));
                templateParamJson = objectMapper.writeValueAsString(templateParam);
            }

            // 使用模板中的模板代码和签名，如果模板不存在则使用配置文件中的
            String templateCodeValue = smsTemplate != null && smsTemplate.getTemplateCode() != null 
                    ? smsTemplate.getTemplateCode() : null;
            String signNameValue = smsTemplate != null && smsTemplate.getSignName() != null 
                    ? smsTemplate.getSignName() : null;

            var response = aliyunSmsClient.sendSms(phoneNumber, templateCodeValue, signNameValue, templateParamJson);
            if (response != null && response.getBody() != null) {
                String code = response.getBody().getCode();
                return "OK".equalsIgnoreCase(code);
            }
            log.warn("阿里云短信响应为空: phoneNumber={}", phoneNumber);
            return false;
        } catch (BusinessException e) {
            // BusinessException直接抛出，不捕获
            throw e;
        } catch (Exception e) {
            log.error("阿里云短信发送失败: phoneNumber={}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 通过腾讯云发送短信
     *
     * @param phoneNumber 手机号
     * @param template 通知模板（包含变量数据）
     * @param smsTemplate 短信模板（数据库模板，包含 templateContent）
     * @return 是否发送成功
     */
    private boolean sendViaTencent(
            final String phoneNumber,
            final com.clientservice.application.dto.NotificationTemplate template,
            final NotificationTemplate smsTemplate) {
        try {
            String[] templateParams;
            
            // 如果数据库中有模板内容，使用模板内容构建参数
            if (smsTemplate != null && smsTemplate.getTemplateContent() != null 
                    && !smsTemplate.getTemplateContent().isEmpty()) {
                // 从模板内容构建参数（数组格式，顺序需与腾讯云平台一致）
                templateParams = buildTencentTemplateParams(smsTemplate.getTemplateContent(), template);
            } else {
                // 兼容旧代码：如果没有模板内容，使用默认格式
                templateParams = new String[]{TemplateVariableReplacer.buildDefaultSmsContent(template)};
            }
            
            // 使用模板中的模板ID和签名，如果模板不存在则使用配置文件中的
            String templateIdValue = smsTemplate != null && smsTemplate.getTemplateCode() != null 
                    ? smsTemplate.getTemplateCode() : null;
            String signNameValue = smsTemplate != null && smsTemplate.getSignName() != null 
                    ? smsTemplate.getSignName() : null;
            
            var response = tencentSmsClient.sendSms(phoneNumber, templateIdValue, signNameValue, templateParams);
            
            if (response != null && response.getSendStatusSet() != null && response.getSendStatusSet().length > 0) {
                String code = response.getSendStatusSet()[0].getCode();
                return "Ok".equalsIgnoreCase(code);
            }
            log.warn("腾讯云短信响应为空或无效: phoneNumber={}", phoneNumber);
            return false;
        } catch (BusinessException e) {
            // BusinessException直接抛出，不捕获
            throw e;
        } catch (Exception e) {
            log.error("腾讯云短信发送失败: phoneNumber={}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 构建短信内容（用于记录）
     *
     * @param template 通知模板（包含变量数据）
     * @param smsTemplate 短信模板（数据库模板）
     * @return 短信内容
     */
    private String buildSmsContent(
            final com.clientservice.application.dto.NotificationTemplate template,
            final NotificationTemplate smsTemplate) {
        // 如果有模板内容，尝试构建内容（主要用于记录）
        if (smsTemplate != null && smsTemplate.getTemplateContent() != null 
                && !smsTemplate.getTemplateContent().isEmpty()) {
            try {
                String templateContent = smsTemplate.getTemplateContent();
                
                // 尝试判断是JSON格式还是数组格式
                String trimmed = templateContent.trim();
                if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                    // JSON格式（阿里云），解析后构建可读内容
                    Map<String, Object> templateMap = objectMapper.readValue(templateContent, 
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Object> entry : templateMap.entrySet()) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        String value = String.valueOf(entry.getValue());
                        // 替换变量
                        value = TemplateVariableReplacer.replaceVariables(value, template);
                        sb.append(entry.getKey()).append(":").append(value);
                    }
                    return sb.toString();
                } else if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    // 数组格式（腾讯云），解析后构建可读内容
                    List<Object> templateList = objectMapper.readValue(templateContent, 
                            new com.fasterxml.jackson.core.type.TypeReference<List<Object>>() {});
                    StringBuilder sb = new StringBuilder();
                    for (Object value : templateList) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        String valueStr = String.valueOf(value);
                        // 替换变量
                        valueStr = TemplateVariableReplacer.replaceVariables(valueStr, template);
                        sb.append(valueStr);
                    }
                    return sb.toString();
                } else {
                    // 普通文本格式，直接替换变量
                    return TemplateVariableReplacer.replaceVariables(templateContent, template);
                }
            } catch (Exception e) {
                log.warn("构建短信内容失败，使用默认格式", e);
            }
        }
        // 默认格式
        return TemplateVariableReplacer.buildDefaultSmsContent(template);
    }

    /**
     * 构建阿里云短信模板参数（JSON格式）
     *
     * @param templateContent 模板内容（JSON格式，如：{"code":"${matterName}","url":"${accessUrl}"}）
     * @param template 通知模板（包含变量数据）
     * @return JSON字符串
     */
    private String buildAliyunTemplateParams(
            final String templateContent,
            final com.clientservice.application.dto.NotificationTemplate template) {
        try {
            // 解析模板内容JSON
            Map<String, Object> templateMap = objectMapper.readValue(templateContent, 
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            
            // 替换变量值
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, Object> entry : templateMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    String valueStr = (String) value;
                    // 替换变量
                    valueStr = TemplateVariableReplacer.replaceVariables(valueStr, template);
                    params.put(key, valueStr);
                } else {
                    params.put(key, String.valueOf(value));
                }
            }
            
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            log.error("构建阿里云短信模板参数失败: templateContent={}", templateContent, e);
            // 降级：使用默认格式
            Map<String, String> templateParam = new HashMap<>();
            String content = TemplateVariableReplacer.buildDefaultSmsContent(template);
            templateParam.put("content", content);
            try {
                return objectMapper.writeValueAsString(templateParam);
            } catch (Exception ex) {
                log.error("构建默认短信参数失败", ex);
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "构建短信参数失败");
            }
        }
    }

    /**
     * 构建腾讯云短信模板参数（数组格式）
     *
     * @param templateContent 模板内容（数组格式，如：["${matterName}","${accessUrl}"]）
     * @param template 通知模板（包含变量数据）
     * @return 字符串数组
     */
    private String[] buildTencentTemplateParams(
            final String templateContent,
            final com.clientservice.application.dto.NotificationTemplate template) {
        try {
            // 解析模板内容数组
            List<Object> templateList = objectMapper.readValue(templateContent, 
                    new com.fasterxml.jackson.core.type.TypeReference<List<Object>>() {});
            
            // 替换变量值
            String[] params = new String[templateList.size()];
            for (int i = 0; i < templateList.size(); i++) {
                Object value = templateList.get(i);
                String valueStr = value != null ? String.valueOf(value) : "";
                // 替换变量
                valueStr = TemplateVariableReplacer.replaceVariables(valueStr, template);
                params[i] = valueStr;
            }
            
            return params;
        } catch (Exception e) {
            log.error("构建腾讯云短信模板参数失败: templateContent={}", templateContent, e);
            // 降级：使用默认格式
            return new String[]{TemplateVariableReplacer.buildDefaultSmsContent(template)};
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
     * 重试发送短信（不创建新记录，用于重试）
     * 注意：重试时使用记录中的内容，不再重新构建模板参数
     *
     * @param phoneNumber 手机号
     * @param content 短信内容（从记录中读取）
     * @return 是否发送成功
     */
    public boolean retrySendSms(final String phoneNumber, final String content) {
        if (!isSmsEnabled()) {
            log.warn("短信通知未启用，跳过重试: phoneNumber={}", phoneNumber);
            return false;
        }

        try {
            // 从数据库获取启用的模板（用于获取模板代码和签名）
            String provider = getProvider();
            List<NotificationTemplate> templates = templateMapper.selectEnabledByTypeAndProvider(
                    NotificationTemplate.TYPE_SMS, provider);
            NotificationTemplate smsTemplate = templates.isEmpty() ? null : templates.get(0);
            
            // 重试时使用默认格式构建参数（因为无法从content还原模板结构）
            // 这里使用兼容方式：构建一个简单的通知模板对象
            com.clientservice.application.dto.NotificationTemplate template = 
                    com.clientservice.application.dto.NotificationTemplate.builder()
                            .clientName("客户")
                            .accessUrl(content.contains("http") ? extractUrl(content) : "")
                            .validDays(30)
                            .build();
            
            boolean sendSuccess = sendSmsByProvider(provider, phoneNumber, template, smsTemplate, null);

            if (sendSuccess) {
                log.info("短信重试发送成功: phoneNumber={}, provider={}", phoneNumber, provider);
            }

            return sendSuccess;

        } catch (Exception e) {
            log.error("短信重试发送失败: phoneNumber={}", phoneNumber, e);
            return false;
        }
    }
    
    /**
     * 从内容中提取URL（简单实现）
     */
    private String extractUrl(final String content) {
        // 简单提取，实际应该使用正则表达式
        int httpIndex = content.indexOf("http");
        if (httpIndex >= 0) {
            String urlPart = content.substring(httpIndex);
            int spaceIndex = urlPart.indexOf(" ");
            if (spaceIndex > 0) {
                return urlPart.substring(0, spaceIndex);
            }
            return urlPart;
        }
        return "";
    }
}
