package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.domain.entity.NotificationTemplate;
import com.clientservice.infrastructure.notification.wechat.WechatAccessTokenService;
import com.clientservice.infrastructure.notification.wechat.WechatApiClient;
import com.clientservice.infrastructure.persistence.mapper.NotificationRecordMapper;
import com.clientservice.infrastructure.persistence.mapper.NotificationTemplateMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 微信通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatNotificationService {

    private final NotificationRecordMapper notificationRecordMapper;
    private final NotificationTemplateMapper templateMapper;
    private final WechatAccessTokenService accessTokenService;
    private final WechatApiClient wechatApiClient;
    private final ObjectMapper objectMapper;
    private final SysConfigService sysConfigService;
    private final CallbackService callbackService;

    @Value("${client-service.notification.wechat.enabled:false}")
    private boolean wechatEnabledDefault;

    /**
     * 获取微信通知是否启用（优先从数据库读取，如果数据库没有则使用配置文件默认值）
     */
    private boolean isWechatEnabled() {
        return sysConfigService.getBooleanConfig("notification.wechat.enabled", wechatEnabledDefault);
    }

    @Value("${client-service.notification.retry.max-retries:3}")
    private int defaultMaxRetries;

    @Value("${client-service.notification.retry.interval-minutes:30}")
    private int retryIntervalMinutes;

    /**
     * 发送微信通知（模板消息）
     *
     * @param matterId 项目ID
     * @param lawFirmMatterId 律所系统项目ID
     * @param clientName 客户名称
     * @param clientId 客户ID
     * @param openId 微信OpenID
     * @param templateId 模板ID
     * @param templateData 模板数据
     * @return 通知记录
     */
    @Transactional
    public NotificationRecord sendWechat(
            final String matterId,
            final Long lawFirmMatterId,
            final String clientName,
            final Long clientId,
            final String openId,
            final String templateId,
            final String templateData) {

        // 检查微信通知是否启用
        if (!isWechatEnabled()) {
            log.info("微信通知未启用，跳过发送: openId={}", openId);
            return null;
        }

        // 创建通知记录（待发送状态）
        NotificationRecord record = NotificationRecord.builder()
                .matterId(matterId)
                .clientId(clientId)
                .lawFirmMatterId(lawFirmMatterId)
                .clientName(clientName)
                .notificationType(NotificationRecord.TYPE_WECHAT)
                .recipient(openId)
                .content(templateId + "|" + templateData) // 保存templateId和templateData，用|分隔
                .status(NotificationRecord.STATUS_PENDING)
                .retryCount(0)
                .maxRetries(defaultMaxRetries)
                .build();

        notificationRecordMapper.insert(record);

        try {
            // 如果templateId为空，尝试从数据库获取启用的模板
            String finalTemplateId = templateId;
            if ((finalTemplateId == null || finalTemplateId.isEmpty()) && accessTokenService.isConfigured()) {
                List<NotificationTemplate> templates = templateMapper.selectEnabledByTypeAndProvider(
                        NotificationTemplate.TYPE_WECHAT, NotificationTemplate.PROVIDER_WECHAT);
                if (!templates.isEmpty()) {
                    finalTemplateId = templates.get(0).getTemplateCode();
                    log.info("使用数据库中的微信模板: templateId={}", finalTemplateId);
                }
            }
            
            boolean sendSuccess = false;
            
            // 检查是否配置了微信参数
            if (accessTokenService.isConfigured()) {
                sendSuccess = sendTemplateMessage(openId, finalTemplateId != null ? finalTemplateId : templateId, templateData);
            } else {
                log.warn("微信通知未配置，使用模拟发送: matterId={}, openId={}", matterId, openId);
                sendSuccess = true; // 模拟发送成功
            }

            if (sendSuccess) {
                record.setStatus(NotificationRecord.STATUS_SUCCESS);
                record.setSentAt(LocalDateTime.now());
                notificationRecordMapper.updateById(record);
                log.info("微信通知发送成功: matterId={}, openId={}", matterId, openId);
                // 回调通知发送成功
                callbackService.callbackNotificationSuccess(record);
            } else {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "微信通知发送失败");
            }

        } catch (Exception e) {
            log.error("微信通知发送失败: matterId={}, openId={}", matterId, openId, e);
            handleNotificationFailure(record, e.getMessage());
            // 回调通知发送失败
            callbackService.callbackNotificationFailure(record);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "微信通知发送失败: " + e.getMessage());
        }

        return record;
    }

    /**
     * 发送模板消息
     *
     * @param openId 用户OpenID
     * @param templateId 模板ID
     * @param templateData 模板数据（JSON字符串）
     * @return 是否发送成功
     */
    private boolean sendTemplateMessage(
            final String openId,
            final String templateId,
            final String templateData) {
        try {
            // 获取AccessToken
            String accessToken = accessTokenService.getAccessToken();
            
            // 构建请求URL
            String url = String.format(
                    "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s",
                    accessToken);
            
            // 构建请求体
            String requestBody = buildTemplateMessageRequest(openId, templateId, templateData);
            
            // 调用微信API
            String response = wechatApiClient.postJson(url, requestBody);
            
            // 解析响应（添加空值检查防止 NPE）
            if (response == null || response.isBlank()) {
                log.error("微信API返回空响应: openId={}, templateId={}", openId, templateId);
                return false;
            }
            JsonNode jsonNode = objectMapper.readTree(response);
            int errcode = jsonNode.has("errcode") ? jsonNode.get("errcode").asInt() : -1;
            String errmsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "";
            
            if (errcode == 0) {
                log.info("微信模板消息发送成功: openId={}, templateId={}", openId, templateId);
                return true;
            } else {
                log.error("微信模板消息发送失败: openId={}, templateId={}, errcode={}, errmsg={}", 
                        openId, templateId, errcode, errmsg);
                
                // 如果AccessToken过期，清除缓存并重试一次
                if (errcode == 40001 || errcode == 40014) {
                    log.warn("微信AccessToken可能过期，清除缓存并重试: errcode={}", errcode);
                    accessTokenService.clearAccessTokenCache();
                    // 这里可以选择重试，但为了避免无限循环，先返回false
                }
                return false;
            }
        } catch (Exception e) {
            log.error("发送微信模板消息异常: openId={}, templateId={}", openId, templateId, e);
            return false;
        }
    }

    /**
     * 构建模板消息请求体
     *
     * @param openId 用户OpenID
     * @param templateId 模板ID
     * @param templateData 模板数据（JSON字符串）
     * @return 请求体JSON字符串
     */
    private String buildTemplateMessageRequest(
            final String openId,
            final String templateId,
            final String templateData) {
        try {
            // 添加空值检查防止 NPE
            if (templateData == null || templateData.isBlank()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "模板数据不能为空");
            }
            // 验证模板数据是否为有效的JSON
            objectMapper.readTree(templateData);
            
            // 构建请求JSON（微信模板消息格式）
            return String.format(
                    "{\"touser\":\"%s\",\"template_id\":\"%s\",\"data\":%s}",
                    openId, templateId, templateData);
        } catch (Exception e) {
            log.error("构建微信模板消息请求体失败: openId={}, templateId={}", openId, templateId, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "构建微信模板消息请求体失败: " + e.getMessage());
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
     * 重试发送微信通知（不创建新记录，用于重试）
     *
     * @param openId 微信OpenID
     * @param templateId 模板ID
     * @param templateData 模板数据
     * @return 是否发送成功
     */
    public boolean retrySendWechat(final String openId, final String templateId, final String templateData) {
        if (!isWechatEnabled()) {
            log.warn("微信通知未启用，跳过重试: openId={}", openId);
            return false;
        }

        try {
            boolean sendSuccess = false;
            
            // 检查是否配置了微信参数
            if (accessTokenService.isConfigured()) {
                sendSuccess = sendTemplateMessage(openId, templateId, templateData);
            } else {
                log.warn("微信通知未配置，使用模拟发送: openId={}", openId);
                sendSuccess = true; // 模拟发送成功
            }

            if (sendSuccess) {
                log.info("微信重试发送成功: openId={}", openId);
            }

            return sendSuccess;

        } catch (Exception e) {
            log.error("微信重试发送失败: openId={}", openId, e);
            return false;
        }
    }
}
