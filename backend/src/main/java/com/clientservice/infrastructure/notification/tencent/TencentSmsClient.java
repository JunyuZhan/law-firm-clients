package com.clientservice.infrastructure.notification.tencent;

import com.clientservice.application.service.SysConfigService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 腾讯云短信客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TencentSmsClient {

    private final SysConfigService sysConfigService;

    @Value("${client-service.notification.sms.tencent.secret-id:}")
    private String secretIdDefault;

    @Value("${client-service.notification.sms.tencent.secret-key:}")
    private String secretKeyDefault;

    @Value("${client-service.notification.sms.tencent.app-id:}")
    private String appIdDefault;

    @Value("${client-service.notification.sms.tencent.sign-name:}")
    private String signNameDefault;

    @Value("${client-service.notification.sms.tencent.template-id:}")
    private String templateIdDefault;

    @Value("${client-service.notification.sms.tencent.region:ap-beijing}")
    private String regionDefault;

    private volatile SmsClient client;

    /**
     * 获取 Secret ID（优先从数据库读取）
     */
    private String getSecretId() {
        return sysConfigService.getConfigValue("client-service.notification.sms.tencent.secret-id", secretIdDefault);
    }

    /**
     * 获取 Secret Key（优先从数据库读取）
     */
    private String getSecretKey() {
        return sysConfigService.getConfigValue("client-service.notification.sms.tencent.secret-key", secretKeyDefault);
    }

    /**
     * 获取 App ID（优先从数据库读取）
     */
    private String getAppId() {
        return sysConfigService.getConfigValue("client-service.notification.sms.tencent.app-id", appIdDefault);
    }

    /**
     * 获取签名名称（优先从数据库读取）
     */
    private String getSignName() {
        return sysConfigService.getConfigValue("client-service.notification.sms.tencent.sign-name", signNameDefault);
    }

    /**
     * 获取模板ID（优先从数据库读取）
     */
    private String getTemplateId() {
        return sysConfigService.getConfigValue("client-service.notification.sms.tencent.template-id", templateIdDefault);
    }

    /**
     * 获取区域（优先从数据库读取）
     */
    private String getRegion() {
        return sysConfigService.getConfigValue("client-service.notification.sms.tencent.region", regionDefault);
    }

    /**
     * 初始化客户端（线程安全，支持配置刷新）
     * 注意：为了支持动态配置，每次都会重新获取配置，但客户端会缓存
     * 如果配置变化，需要清除客户端缓存（通过重新设置 client = null）
     */
    private SmsClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    try {
                        // 重新获取配置（支持从数据库读取）
                        String secretId = getSecretId();
                        String secretKey = getSecretKey();
                        String region = getRegion();
                        
                        Credential cred = new Credential(secretId, secretKey);
                        HttpProfile httpProfile = new HttpProfile();
                        httpProfile.setEndpoint("sms.tencentcloudapi.com");
                        ClientProfile clientProfile = new ClientProfile();
                        clientProfile.setHttpProfile(httpProfile);
                        client = new SmsClient(cred, region, clientProfile);
                        log.info("腾讯云短信客户端已初始化");
                    } catch (Exception e) {
                        log.error("初始化腾讯云短信客户端失败", e);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "初始化腾讯云短信客户端失败: " + e.getMessage());
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
            log.info("腾讯云短信客户端缓存已清除");
        }
    }

    /**
     * 发送短信
     *
     * @param phoneNumber 手机号
     * @param templateParams 模板参数数组
     * @return 发送结果
     */
    public SendSmsResponse sendSms(final String phoneNumber, final String[] templateParams) {
        return sendSms(phoneNumber, getTemplateId(), getSignName(), templateParams);
    }

    /**
     * 发送短信（支持动态模板ID和签名）
     *
     * @param phoneNumber 手机号
     * @param templateIdValue 模板ID
     * @param signNameValue 签名名称
     * @param templateParams 模板参数数组
     * @return 发送结果
     */
    public SendSmsResponse sendSms(
            final String phoneNumber,
            final String templateIdValue,
            final String signNameValue,
            final String[] templateParams) {
        try {
            SmsClient client = getClient();
            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(getAppId());
            req.setSignName(signNameValue != null ? signNameValue : getSignName());
            req.setTemplateId(templateIdValue != null ? templateIdValue : getTemplateId());
            req.setPhoneNumberSet(new String[]{phoneNumber});
            req.setTemplateParamSet(templateParams);

            SendSmsResponse resp = client.SendSms(req);
            
            if (resp != null && resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                log.info("腾讯云短信发送响应: phoneNumber={}, code={}, message={}", 
                        phoneNumber, resp.getSendStatusSet()[0].getCode(), resp.getSendStatusSet()[0].getMessage());
            } else {
                log.warn("腾讯云短信发送响应为空或无效: phoneNumber={}", phoneNumber);
            }

            return resp;
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云短信发送异常: phoneNumber={}", phoneNumber, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "腾讯云短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 检查配置是否完整
     *
     * @return 是否配置完整
     */
    public boolean isConfigured() {
        String secretId = getSecretId();
        String secretKey = getSecretKey();
        String appId = getAppId();
        String signName = getSignName();
        String templateId = getTemplateId();
        return secretId != null && !secretId.isEmpty()
                && secretKey != null && !secretKey.isEmpty()
                && appId != null && !appId.isEmpty()
                && signName != null && !signName.isEmpty()
                && templateId != null && !templateId.isEmpty();
    }
}
