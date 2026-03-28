package com.clientservice.infrastructure.notification.aliyun;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.clientservice.application.service.SysConfigService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AliyunSmsClient {

    private final SysConfigService sysConfigService;

    @Value("${client-service.notification.sms.aliyun.access-key-id:}")
    private String accessKeyIdDefault;

    @Value("${client-service.notification.sms.aliyun.access-key-secret:}")
    private String accessKeySecretDefault;

    @Value("${client-service.notification.sms.aliyun.sign-name:}")
    private String signNameDefault;

    @Value("${client-service.notification.sms.aliyun.template-code:}")
    private String templateCodeDefault;

    @Value("${client-service.notification.sms.aliyun.endpoint:dysmsapi.aliyuncs.com}")
    private String endpointDefault;

    private volatile Client client;

    /**
     * 获取 Access Key ID（优先从数据库读取）
     */
    private String getAccessKeyId() {
        return sysConfigService.getConfigValue("client-service.notification.sms.aliyun.access-key-id", accessKeyIdDefault);
    }

    /**
     * 获取 Access Key Secret（优先从数据库读取）
     */
    private String getAccessKeySecret() {
        return sysConfigService.getConfigValue("client-service.notification.sms.aliyun.access-key-secret", accessKeySecretDefault);
    }

    /**
     * 获取签名名称（优先从数据库读取）
     */
    private String getSignName() {
        return sysConfigService.getConfigValue("client-service.notification.sms.aliyun.sign-name", signNameDefault);
    }

    /**
     * 获取模板代码（优先从数据库读取）
     */
    private String getTemplateCode() {
        return sysConfigService.getConfigValue("client-service.notification.sms.aliyun.template-code", templateCodeDefault);
    }

    /**
     * 获取端点（优先从数据库读取）
     */
    private String getEndpoint() {
        return sysConfigService.getConfigValue("client-service.notification.sms.aliyun.endpoint", endpointDefault);
    }

    /**
     * 初始化客户端（线程安全，支持配置刷新）
     * 注意：为了支持动态配置，每次都会重新获取配置，但客户端会缓存
     * 如果配置变化，需要清除客户端缓存（通过重新设置 client = null）
     */
    private Client getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    try {
                        // 重新获取配置（支持从数据库读取）
                        String accessKeyId = getAccessKeyId();
                        String accessKeySecret = getAccessKeySecret();
                        String endpoint = getEndpoint();
                        
                        Config config = new Config()
                                .setAccessKeyId(accessKeyId)
                                .setAccessKeySecret(accessKeySecret)
                                .setEndpoint(endpoint);
                        client = new Client(config);
                        log.info("阿里云短信客户端已初始化");
                    } catch (Exception e) {
                        log.error("初始化阿里云短信客户端失败", e);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "初始化阿里云短信客户端失败: " + e.getMessage());
                    }
                }
            }
        }
        return client;
    }

    /**
     * 清除客户端缓存（用于配置更新后刷新客户端）
     */
    public void clearClientCache() {
        synchronized (this) {
            client = null;
            log.info("阿里云短信客户端缓存已清除");
        }
    }

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号
     * @param templateParam 模板参数（JSON字符串）
     * @return 发送结果
     */
    public SendSmsResponse sendSms(final String phoneNumber, final String templateParam) {
        return sendSms(phoneNumber, getTemplateCode(), getSignName(), templateParam);
    }

    /**
     * 发送短信（支持动态模板代码和签名）
     *
     * @param phoneNumber 手机号
     * @param templateCodeValue 模板代码
     * @param signNameValue 签名名称
     * @param templateParam 模板参数（JSON字符串）
     * @return 发送结果
     */
    public SendSmsResponse sendSms(
            final String phoneNumber,
            final String templateCodeValue,
            final String signNameValue,
            final String templateParam) {
        try {
            Client client = getClient();
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(signNameValue != null ? signNameValue : getSignName())
                    .setTemplateCode(templateCodeValue != null ? templateCodeValue : getTemplateCode())
                    .setTemplateParam(templateParam);

            SendSmsResponse response = client.sendSms(request);
            
            if (response != null && response.getBody() != null) {
                log.info("阿里云短信发送响应: phoneNumber={}, code={}, message={}", 
                        phoneNumber, response.getBody().getCode(), response.getBody().getMessage());
            } else {
                log.warn("阿里云短信发送响应为空: phoneNumber={}", phoneNumber);
            }

            return response;
        } catch (Exception e) {
            log.error("阿里云短信发送异常: phoneNumber={}", phoneNumber, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "阿里云短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 检查配置是否完整
     *
     * @return 是否配置完整
     */
    public boolean isConfigured() {
        String accessKeyId = getAccessKeyId();
        String accessKeySecret = getAccessKeySecret();
        String signName = getSignName();
        String templateCode = getTemplateCode();
        return accessKeyId != null && !accessKeyId.isEmpty()
                && accessKeySecret != null && !accessKeySecret.isEmpty()
                && signName != null && !signName.isEmpty()
                && templateCode != null && !templateCode.isEmpty();
    }
}
