package com.clientservice.application.service;

import com.clientservice.common.util.UrlGenerator;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.ClientFile;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 回调服务 - 将访问日志和下载日志回调给管理系统
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private final ClientMatterMapper matterMapper;
    private final RestTemplate restTemplate;
    private final SysConfigService sysConfigService;
    private final UrlGenerator urlGenerator;

    /** 管理系统回调地址（配置文件中的默认值） */
    @Value("${client-service.callback.law-firm-url:}")
    private String defaultLawFirmCallbackUrl;

    /** 是否启用回调（配置文件中的默认值） */
    @Value("${client-service.callback.enabled:true}")
    private boolean defaultCallbackEnabled;

    /** 回调 API Key（用于律所系统验证回调请求的合法性） */
    @Value("${client-service.callback.api-key:}")
    private String defaultCallbackApiKey;

    /** 是否允许回调到内网地址（默认 true，企业内网部署场景需要允许） */
    @Value("${client-service.callback.allow-internal:true}")
    private boolean defaultAllowInternalCallback;

    /** 回调最大重试次数 */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /** 重试间隔（毫秒） */
    private static final long RETRY_DELAY_MS = 1000;

    /** 律所系统 API 前缀 */
    private static final String LAW_FIRM_API_PREFIX = "/api";

    /**
     * 检查回调是否启用（优先从系统配置读取）
     *
     * @return 是否启用
     */
    private boolean isCallbackEnabled() {
        return sysConfigService.getBooleanConfig("callback.enabled", defaultCallbackEnabled);
    }

    /**
     * 获取管理系统回调地址（优先从系统配置读取）
     *
     * @return 回调地址
     */
    private String getLawFirmCallbackUrl() {
        return sysConfigService.getConfigValue("callback.law-firm-url", defaultLawFirmCallbackUrl);
    }

    /**
     * 获取回调 API Key（优先从系统配置读取）
     * 用于律所系统验证回调请求的合法性
     *
     * @return API Key，如果未配置则返回空字符串
     */
    private String getCallbackApiKey() {
        return sysConfigService.getConfigValue("callback.api-key", defaultCallbackApiKey);
    }

    /**
     * 检查是否允许回调到内网地址（优先从系统配置读取）
     * 企业内网部署场景通常需要允许内网回调
     *
     * @return 是否允许内网回调
     */
    private boolean isAllowInternalCallback() {
        return sysConfigService.getBooleanConfig("callback.allow-internal", defaultAllowInternalCallback);
    }

    /**
     * 异步回调访问日志给管理系统
     *
     * @param accessLog 访问日志
     */
    @Async
    public void callbackAccessLog(final AccessLog accessLog) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过访问日志回调");
            return;
        }

        String callbackUrl = getLawFirmCallbackUrl();
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过访问日志回调");
            return;
        }

        try {
            // 获取项目信息
            ClientMatter matter = matterMapper.selectById(accessLog.getMatterId());
            if (matter == null) {
                log.warn("项目不存在，跳过访问日志回调: matterId={}", accessLog.getMatterId());
                return;
            }

            // 构建回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", matter.getLawFirmMatterId()); // 律所系统的项目ID
            callbackData.put("clientId", accessLog.getClientId());
            // LocalDateTime 序列化为 ISO 格式字符串（Spring Boot 的 RestTemplate 会自动处理）
            callbackData.put("accessTime", accessLog.getAccessTime() != null 
                    ? accessLog.getAccessTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);
            callbackData.put("ipAddress", accessLog.getIpAddress());
            callbackData.put("userAgent", accessLog.getUserAgent());
            callbackData.put("eventType", "ACCESS"); // 事件类型：访问

            // 发送回调请求
            String fullCallbackUrl = buildLawFirmCallbackUrl(callbackUrl, "/open/client/access-log");
            sendCallback(fullCallbackUrl, callbackData);

            log.debug("访问日志回调成功: matterId={}, clientId={}", 
                    accessLog.getMatterId(), accessLog.getClientId());

        } catch (Exception e) {
            log.error("访问日志回调失败: matterId={}", accessLog.getMatterId(), e);
            // 回调失败不影响主流程，只记录日志
        }
    }

    /**
     * 异步回调下载日志给管理系统
     *
     * @param downloadLog 下载日志
     */
    @Async
    public void callbackDownloadLog(final DownloadLog downloadLog) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过下载日志回调");
            return;
        }

        String callbackUrl = getLawFirmCallbackUrl();
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过下载日志回调");
            return;
        }

        try {
            // 获取项目信息
            ClientMatter matter = matterMapper.selectById(downloadLog.getMatterId());
            if (matter == null) {
                log.warn("项目不存在，跳过下载日志回调: matterId={}", downloadLog.getMatterId());
                return;
            }

            // 构建回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", matter.getLawFirmMatterId()); // 律所系统的项目ID
            callbackData.put("clientId", downloadLog.getClientId());
            callbackData.put("fileId", downloadLog.getFileId());
            callbackData.put("fileName", downloadLog.getFileName());
            // LocalDateTime 序列化为 ISO 格式字符串（Spring Boot 的 RestTemplate 会自动处理）
            callbackData.put("downloadTime", downloadLog.getDownloadTime() != null 
                    ? downloadLog.getDownloadTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);
            callbackData.put("ipAddress", downloadLog.getIpAddress());
            callbackData.put("userAgent", downloadLog.getUserAgent());
            callbackData.put("eventType", "DOWNLOAD"); // 事件类型：下载

            // 发送回调请求
            String fullCallbackUrl = buildLawFirmCallbackUrl(callbackUrl, "/open/client/download-log");
            sendCallback(fullCallbackUrl, callbackData);

            log.debug("下载日志回调成功: matterId={}, fileId={}", 
                    downloadLog.getMatterId(), downloadLog.getFileId());

        } catch (Exception e) {
            log.error("下载日志回调失败: matterId={}, fileId={}", 
                    downloadLog.getMatterId(), downloadLog.getFileId(), e);
            // 回调失败不影响主流程，只记录日志
        }
    }

    /**
     * 异步回调文件上传通知给管理系统
     *
     * @param file 文件实体
     * @param matter 项目实体
     * @param token 访问令牌（用于生成文件下载URL）
     */
    @Async
    public void callbackUploadFile(final ClientFile file, final ClientMatter matter, final String token) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过文件上传回调");
            return;
        }

        String callbackUrl = getLawFirmCallbackUrl();
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过文件上传回调");
            return;
        }

        try {
            // 生成文件下载URL
            String fileDownloadUrl = urlGenerator.generateFileDownloadUrl(
                    file.getId(), file.getMatterId(), token);

            // 构建回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", matter.getLawFirmMatterId()); // 律所系统的项目ID
            callbackData.put("clientId", file.getClientId() != null ? file.getClientId() : matter.getClientId());
            callbackData.put("clientName", matter.getClientName());
            callbackData.put("fileName", file.getFileName());
            callbackData.put("fileSize", file.getFileSize());
            callbackData.put("fileType", file.getFileType());
            callbackData.put("fileCategory", file.getFileCategory());
            callbackData.put("description", file.getDescription());
            callbackData.put("externalFileId", file.getId()); // 客户服务系统中的文件ID
            callbackData.put("externalFileUrl", fileDownloadUrl); // 文件下载URL
            callbackData.put("uploadedBy", matter.getClientName()); // 上传人姓名（使用客户姓名）
            callbackData.put("uploadedAt", file.getUploadedAt() != null 
                    ? file.getUploadedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // 发送回调请求
            String fullCallbackUrl = buildLawFirmCallbackUrl(callbackUrl, "/open/client/files");
            sendCallback(fullCallbackUrl, callbackData);

            log.debug("文件上传回调成功: fileId={}, matterId={}", 
                    file.getId(), file.getMatterId());

        } catch (Exception e) {
            log.error("文件上传回调失败: fileId={}, matterId={}", 
                    file.getId(), file.getMatterId(), e);
            // 回调失败不影响主流程，只记录日志
        }
    }

    /**
     * 发送回调请求（带重试机制）
     *
     * @param url 回调URL
     * @param data 回调数据
     */
    /**
     * 验证回调 URL 安全性
     * 当 callback.allow-internal=false 时，会阻止回调到内网地址（防止 SSRF 攻击）
     * 企业内网部署场景（callback.allow-internal=true）允许内网回调
     */
    private void validateCallbackUrl(final String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("回调地址不能为空");
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                throw new IllegalArgumentException("回调地址仅允许 http/https 协议");
            }
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("回调地址缺少主机名");
            }
            // 如果允许内网回调，跳过内网检查（企业内网部署场景）
            if (isAllowInternalCallback()) {
                log.debug("允许内网回调已启用，跳过内网地址检查: {}", host);
                return;
            }
            // 检查是否为内网地址
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                throw new IllegalArgumentException("回调地址不允许指向内网（如需内网回调，请设置 callback.allow-internal=true）");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("回调地址无效: " + e.getMessage());
        }
    }

    private void sendCallback(final String url, final Map<String, Object> data) {
        // SSRF 防护：验证回调 URL 不指向内网
        validateCallbackUrl(url);

        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                doSendCallback(url, data);
                return; // 成功则直接返回
            } catch (Exception e) {
                lastException = e;
                log.warn("回调请求失败，第{}次尝试: url={}, error={}", 
                        attempt, url, e.getMessage());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        // 使用指数退避策略
                        long delayMs = RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1);
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("回调重试被中断: url={}", url);
                        break;
                    }
                }
            }
        }
        
        // 所有重试都失败
        log.error("回调请求在{}次重试后仍然失败: url={}", MAX_RETRY_ATTEMPTS, url, lastException);
        if (lastException != null) {
            throw new RuntimeException("回调失败: " + lastException.getMessage(), lastException);
        }
    }

    /**
     * 实际发送回调请求
     *
     * @param url 回调URL
     * @param data 回调数据
     */
    private String buildLawFirmCallbackUrl(final String baseUrl, final String endpointPath) {
        String base = baseUrl == null ? "" : baseUrl.trim();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        if (base.endsWith(LAW_FIRM_API_PREFIX)) {
            return base + endpointPath;
        }
        return base + LAW_FIRM_API_PREFIX + endpointPath;
    }

    private void doSendCallback(final String url, final Map<String, Object> data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 添加回调 API Key（如果配置了）
        String apiKey = getCallbackApiKey();
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-Callback-Key", apiKey);
            headers.set("Authorization", "Bearer " + apiKey);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.debug("回调请求成功: url={}", url);
        } else {
            throw new RuntimeException("回调请求返回非成功状态: " + response.getStatusCode());
        }
    }

    /**
     * 异步回调通知发送成功给管理系统
     *
     * @param record 通知记录
     */
    @Async
    public void callbackNotificationSuccess(final NotificationRecord record) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过通知成功回调");
            return;
        }

        String callbackUrl = getLawFirmCallbackUrl();
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过通知成功回调");
            return;
        }

        try {
            // 构建回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", record.getLawFirmMatterId() != null ? record.getLawFirmMatterId() : "");
            callbackData.put("clientId", record.getClientId() != null ? record.getClientId() : 0L);
            callbackData.put("clientName", record.getClientName() != null ? record.getClientName() : "");
            callbackData.put("notificationType", record.getNotificationType());
            callbackData.put("recipient", record.getRecipient() != null ? record.getRecipient() : "");
            callbackData.put("status", record.getStatus());
            callbackData.put("sentAt", record.getSentAt() != null
                    ? record.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);
            callbackData.put("eventType", "NOTIFICATION_SUCCESS"); // 事件类型：通知成功

            // 发送回调请求
            String fullCallbackUrl =
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/notification-success");
            sendCallback(fullCallbackUrl, callbackData);

            log.info("通知成功回调成功: matterId={}, notificationType={}",
                    record.getMatterId(), record.getNotificationType());

        } catch (Exception e) {
            log.error("通知成功回调失败: matterId={}", record.getMatterId(), e);
            // 回调失败不影响主流程，只记录日志
        }
    }

    /**
     * 异步回调通知发送失败给管理系统
     *
     * @param record 通知记录
     */
    @Async
    public void callbackNotificationFailure(final NotificationRecord record) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过通知失败回调");
            return;
        }

        String callbackUrl = getLawFirmCallbackUrl();
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过通知失败回调");
            return;
        }

        try {
            // 构建回调数据
            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", record.getLawFirmMatterId() != null ? record.getLawFirmMatterId() : "");
            callbackData.put("clientId", record.getClientId() != null ? record.getClientId() : 0L);
            callbackData.put("clientName", record.getClientName() != null ? record.getClientName() : "");
            callbackData.put("notificationType", record.getNotificationType());
            callbackData.put("recipient", record.getRecipient() != null ? record.getRecipient() : "");
            callbackData.put("status", record.getStatus());
            callbackData.put("errorCode", record.getErrorCode() != null ? record.getErrorCode() : "");
            callbackData.put("errorMessage", record.getErrorMessage() != null ? record.getErrorMessage() : "");
            callbackData.put("sentAt", record.getSentAt() != null
                    ? record.getSentAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);
            callbackData.put("eventType", "NOTIFICATION_FAILURE"); // 事件类型：通知失败

            // 发送回调请求
            String fullCallbackUrl =
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/notification-failure");
            sendCallback(fullCallbackUrl, callbackData);

            log.info("通知失败回调成功: matterId={}, notificationType={}, errorCode={}",
                    record.getMatterId(), record.getNotificationType(), record.getErrorCode());

        } catch (Exception e) {
            log.error("通知失败回调失败: matterId={}", record.getMatterId(), e);
            // 回调失败不影响主流程，只记录日志
        }
    }
}
