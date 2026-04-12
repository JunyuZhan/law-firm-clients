package com.clientservice.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.clientservice.common.util.UrlGenerator;
import com.clientservice.domain.entity.AccessLog;
import com.clientservice.domain.entity.ApiKey;
import com.clientservice.domain.entity.CallbackTask;
import com.clientservice.domain.entity.ClientFile;
import com.clientservice.domain.entity.ClientMatter;
import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.domain.entity.NotificationRecord;
import com.clientservice.infrastructure.persistence.mapper.ApiKeyMapper;
import com.clientservice.infrastructure.persistence.mapper.CallbackTaskMapper;
import com.clientservice.infrastructure.persistence.mapper.ClientMatterMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestTemplate;

/**
 * 回调服务 - 将访问日志、下载日志、文件上传与通知结果可靠回调给管理系统。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackService {

    private static final String CALLBACK_URL_KEY = "_lawFirmCallbackUrl";
    private static final String SOURCE_API_KEY_ID_KEY = "_sourceApiKeyId";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final int MAX_ERROR_LENGTH = 1000;
    private static final String LAW_FIRM_API_PREFIX = "/api";
    private static final String CALLBACK_NONCE_HEADER = "X-Callback-Nonce";
    private static final String CALLBACK_TIMESTAMP_HEADER = "X-Callback-Timestamp";
    private static final String CALLBACK_SIGNATURE_HEADER = "X-Callback-Signature";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final ClientMatterMapper matterMapper;
    private final ApiKeyMapper apiKeyMapper;
    private final CallbackTaskMapper callbackTaskMapper;
    private final RestTemplate restTemplate;
    private final SysConfigService sysConfigService;
    private final UrlGenerator urlGenerator;
    private final ObjectMapper objectMapper;
    private final TaskExecutor taskExecutor;

    @Value("${client-service.callback.law-firm-url:}")
    private String defaultLawFirmCallbackUrl;

    @Value("${client-service.callback.enabled:true}")
    private boolean defaultCallbackEnabled;

    @Value("${client-service.callback.api-key:}")
    private String defaultCallbackApiKey;

    @Value("${client-service.callback.allow-internal:true}")
    private boolean defaultAllowInternalCallback;

    @Value("${client-service.callback.outbox.max-retries:12}")
    private int defaultOutboxMaxRetries;

    @Value("${client-service.callback.outbox.retry-delay-minutes:5}")
    private int outboxRetryDelayMinutes;

    @Value("${client-service.callback.outbox.batch-size:100}")
    private int outboxBatchSize;

    @Value("${client-service.callback.outbox.stale-sending-minutes:10}")
    private int staleSendingMinutes;

    private boolean isCallbackEnabled() {
        return sysConfigService.getBooleanConfig("callback.enabled", defaultCallbackEnabled);
    }

    private String getLawFirmCallbackUrl() {
        return sysConfigService.getConfigValue("callback.law-firm-url", defaultLawFirmCallbackUrl);
    }

    private String getCallbackApiKey() {
        return sysConfigService.getConfigValue("callback.api-key", defaultCallbackApiKey);
    }

    private boolean isAllowInternalCallback() {
        return sysConfigService.getBooleanConfig("callback.allow-internal", defaultAllowInternalCallback);
    }

    public void callbackAccessLog(final AccessLog accessLog) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过访问日志回调");
            return;
        }

        try {
            ClientMatter matter = matterMapper.selectById(accessLog.getMatterId());
            if (matter == null) {
                log.warn("项目不存在，跳过访问日志回调: matterId={}", accessLog.getMatterId());
                return;
            }
            String callbackUrl = resolveLawFirmCallbackUrl(matter);
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                log.debug("未配置回调地址，跳过访问日志回调");
                return;
            }

            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", matter.getLawFirmMatterId());
            callbackData.put("clientId", accessLog.getClientId());
            callbackData.put("accessTime", accessLog.getAccessTime() != null
                    ? accessLog.getAccessTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);
            callbackData.put("ipAddress", accessLog.getIpAddress());
            callbackData.put("userAgent", accessLog.getUserAgent());
            callbackData.put("eventType", "ACCESS");

            enqueueCallbackTask(
                    accessLog.getMatterId(),
                    CallbackTask.TYPE_ACCESS_LOG,
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/access-log"),
                    callbackData);
            log.debug("访问日志回调已入队: matterId={}, clientId={}",
                    accessLog.getMatterId(), accessLog.getClientId());
        } catch (Exception e) {
            log.error("访问日志回调入队失败: matterId={}", accessLog.getMatterId(), e);
        }
    }

    public void callbackDownloadLog(final DownloadLog downloadLog) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过下载日志回调");
            return;
        }

        try {
            ClientMatter matter = matterMapper.selectById(downloadLog.getMatterId());
            if (matter == null) {
                log.warn("项目不存在，跳过下载日志回调: matterId={}", downloadLog.getMatterId());
                return;
            }
            String callbackUrl = resolveLawFirmCallbackUrl(matter);
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                log.debug("未配置回调地址，跳过下载日志回调");
                return;
            }

            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", matter.getLawFirmMatterId());
            callbackData.put("clientId", downloadLog.getClientId());
            callbackData.put("fileId", downloadLog.getFileId());
            callbackData.put("fileName", downloadLog.getFileName());
            callbackData.put("downloadTime", downloadLog.getDownloadTime() != null
                    ? downloadLog.getDownloadTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : null);
            callbackData.put("ipAddress", downloadLog.getIpAddress());
            callbackData.put("userAgent", downloadLog.getUserAgent());
            callbackData.put("eventType", "DOWNLOAD");

            enqueueCallbackTask(
                    downloadLog.getMatterId(),
                    CallbackTask.TYPE_DOWNLOAD_LOG,
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/download-log"),
                    callbackData);
            log.debug("下载日志回调已入队: matterId={}, fileId={}",
                    downloadLog.getMatterId(), downloadLog.getFileId());
        } catch (Exception e) {
            log.error("下载日志回调入队失败: matterId={}, fileId={}",
                    downloadLog.getMatterId(), downloadLog.getFileId(), e);
        }
    }

    public void callbackUploadFile(final ClientFile file, final ClientMatter matter, final String token) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过文件上传回调");
            return;
        }

        try {
            String callbackUrl = resolveLawFirmCallbackUrl(matter);
            if (callbackUrl == null || callbackUrl.isEmpty()) {
                log.debug("未配置回调地址，跳过文件上传回调");
                return;
            }

            String fileDownloadUrl = urlGenerator.generateFileDownloadUrl(file.getId(), file.getMatterId(), token);

            Map<String, Object> callbackData = new HashMap<>();
            callbackData.put("matterId", matter.getLawFirmMatterId());
            callbackData.put("clientId", file.getClientId() != null ? file.getClientId() : matter.getClientId());
            callbackData.put("clientName", matter.getClientName());
            callbackData.put("fileName", file.getFileName());
            callbackData.put("fileSize", file.getFileSize());
            callbackData.put("fileType", file.getFileType());
            callbackData.put("fileCategory", file.getFileCategory());
            callbackData.put("description", file.getDescription());
            callbackData.put("externalFileId", file.getId());
            callbackData.put("externalFileUrl", fileDownloadUrl);
            callbackData.put("uploadedBy", matter.getClientName());
            callbackData.put("uploadedAt", file.getUploadedAt() != null
                    ? file.getUploadedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            enqueueCallbackTask(
                    file.getMatterId(),
                    CallbackTask.TYPE_FILE_UPLOAD,
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/files"),
                    callbackData);
            log.debug("文件上传回调已入队: fileId={}, matterId={}", file.getId(), file.getMatterId());
        } catch (Exception e) {
            log.error("文件上传回调入队失败: fileId={}, matterId={}", file.getId(), file.getMatterId(), e);
        }
    }

    public void callbackNotificationSuccess(final NotificationRecord record) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过通知成功回调");
            return;
        }

        ClientMatter matter = record.getMatterId() != null ? matterMapper.selectById(record.getMatterId()) : null;
        String callbackUrl = resolveLawFirmCallbackUrl(matter);
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过通知成功回调");
            return;
        }

        try {
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
            callbackData.put("eventType", "NOTIFICATION_SUCCESS");

            enqueueCallbackTask(
                    record.getMatterId(),
                    CallbackTask.TYPE_NOTIFICATION_SUCCESS,
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/notification-success"),
                    callbackData);
            log.info("通知成功回调已入队: matterId={}, notificationType={}",
                    record.getMatterId(), record.getNotificationType());
        } catch (Exception e) {
            log.error("通知成功回调入队失败: matterId={}", record.getMatterId(), e);
        }
    }

    public void callbackNotificationFailure(final NotificationRecord record) {
        if (!isCallbackEnabled()) {
            log.debug("回调未启用，跳过通知失败回调");
            return;
        }

        ClientMatter matter = record.getMatterId() != null ? matterMapper.selectById(record.getMatterId()) : null;
        String callbackUrl = resolveLawFirmCallbackUrl(matter);
        if (callbackUrl == null || callbackUrl.isEmpty()) {
            log.debug("未配置回调地址，跳过通知失败回调");
            return;
        }

        try {
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
            callbackData.put("eventType", "NOTIFICATION_FAILURE");

            enqueueCallbackTask(
                    record.getMatterId(),
                    CallbackTask.TYPE_NOTIFICATION_FAILURE,
                    buildLawFirmCallbackUrl(callbackUrl, "/open/client/notification-failure"),
                    callbackData);
            log.info("通知失败回调已入队: matterId={}, notificationType={}, errorCode={}",
                    record.getMatterId(), record.getNotificationType(), record.getErrorCode());
        } catch (Exception e) {
            log.error("通知失败回调入队失败: matterId={}", record.getMatterId(), e);
        }
    }

    public void retryPendingCallbacks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reclaimBefore = now.minusMinutes(staleSendingMinutes);

        QueryWrapper<CallbackTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                        .eq("status", CallbackTask.STATUS_PENDING)
                        .and(inner -> inner.isNull("next_retry_at").or().le("next_retry_at", now)))
                .or(wrapper -> wrapper
                        .eq("status", CallbackTask.STATUS_SENDING)
                        .le("updated_at", reclaimBefore))
                .orderByAsc("id")
                .last("LIMIT " + Math.max(outboxBatchSize, 1));

        for (CallbackTask task : callbackTaskMapper.selectList(queryWrapper)) {
            deliverTask(task.getId());
        }
    }

    private void enqueueCallbackTask(
            final String matterId,
            final String callbackType,
            final String callbackUrl,
            final Map<String, Object> payload) {
        CallbackTask task = CallbackTask.builder()
                .matterId(matterId)
                .callbackType(callbackType)
                .callbackUrl(callbackUrl)
                .callbackPayload(serializePayload(payload))
                .status(CallbackTask.STATUS_PENDING)
                .retryCount(0)
                .maxRetries(defaultOutboxMaxRetries)
                .build();
        callbackTaskMapper.insert(task);
        scheduleAsyncDelivery(task.getId());
    }

    private void scheduleAsyncDelivery(final Long taskId) {
        Runnable delivery = () -> {
            try {
                deliverTask(taskId);
            } catch (Exception e) {
                log.error("异步投递回调任务失败: taskId={}", taskId, e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    taskExecutor.execute(delivery);
                }
            });
            return;
        }

        taskExecutor.execute(delivery);
    }

    private void deliverTask(final Long taskId) {
        CallbackTask task = callbackTaskMapper.selectById(taskId);
        if (task == null || CallbackTask.STATUS_SENT.equals(task.getStatus())
                || CallbackTask.STATUS_DEAD.equals(task.getStatus())) {
            return;
        }
        if (!claimTask(task)) {
            return;
        }

        try {
            ClientMatter matter = loadMatter(task.getMatterId());
            sendCallback(task.getCallbackUrl(), deserializePayload(task.getCallbackPayload()),
                    resolveCallbackApiKey(matter));
            markTaskSent(task.getId());
            log.info("回调任务投递成功: taskId={}, type={}", task.getId(), task.getCallbackType());
        } catch (Exception e) {
            handleTaskFailure(task, e);
        }
    }

    private ClientMatter loadMatter(final String matterId) {
        if (matterId == null || matterId.isBlank()) {
            return null;
        }
        return matterMapper.selectById(matterId);
    }

    private boolean claimTask(final CallbackTask task) {
        LocalDateTime reclaimBefore = LocalDateTime.now().minusMinutes(staleSendingMinutes);
        if (CallbackTask.STATUS_PENDING.equals(task.getStatus())) {
            return updateTaskStatus(task, CallbackTask.STATUS_PENDING);
        }
        if (CallbackTask.STATUS_SENDING.equals(task.getStatus())
                && task.getUpdatedAt() != null
                && !task.getUpdatedAt().isAfter(reclaimBefore)) {
            return updateTaskStatus(task, CallbackTask.STATUS_SENDING);
        }
        return false;
    }

    private boolean updateTaskStatus(final CallbackTask task, final String currentStatus) {
        CallbackTask update = new CallbackTask();
        update.setStatus(CallbackTask.STATUS_SENDING);
        update.setLastAttemptAt(LocalDateTime.now());
        update.setLastError(null);
        update.setNextRetryAt(null);

        UpdateWrapper<CallbackTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", task.getId())
                .eq("status", currentStatus)
                .eq("updated_at", task.getUpdatedAt());
        return callbackTaskMapper.update(update, wrapper) > 0;
    }

    private void markTaskSent(final Long taskId) {
        CallbackTask update = new CallbackTask();
        update.setStatus(CallbackTask.STATUS_SENT);
        update.setLastError(null);
        update.setNextRetryAt(null);

        UpdateWrapper<CallbackTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", taskId);
        callbackTaskMapper.update(update, wrapper);
    }

    private void handleTaskFailure(final CallbackTask task, final Exception exception) {
        int failedAttempts = (task.getRetryCount() == null ? 0 : task.getRetryCount()) + 1;
        int maxRetries = task.getMaxRetries() == null ? defaultOutboxMaxRetries : task.getMaxRetries();

        CallbackTask update = new CallbackTask();
        update.setRetryCount(failedAttempts);
        update.setLastAttemptAt(LocalDateTime.now());
        update.setLastError(abbreviateError(exception));

        if (failedAttempts >= maxRetries) {
            update.setStatus(CallbackTask.STATUS_DEAD);
            update.setNextRetryAt(null);
            log.error("回调任务达到最大重试次数: taskId={}, type={}",
                    task.getId(), task.getCallbackType(), exception);
        } else {
            update.setStatus(CallbackTask.STATUS_PENDING);
            update.setNextRetryAt(LocalDateTime.now().plusMinutes(calculateRetryDelayMinutes(failedAttempts)));
            log.warn("回调任务投递失败，等待补偿重试: taskId={}, type={}, retryCount={}",
                    task.getId(), task.getCallbackType(), failedAttempts, exception);
        }

        UpdateWrapper<CallbackTask> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", task.getId());
        callbackTaskMapper.update(update, wrapper);
    }

    private int calculateRetryDelayMinutes(final int failedAttempts) {
        int baseDelay = Math.max(outboxRetryDelayMinutes, 1);
        return baseDelay * (int) Math.pow(2, Math.max(failedAttempts - 1, 0));
    }

    private String serializePayload(final Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("序列化回调载荷失败", e);
        }
    }

    private Map<String, Object> deserializePayload(final String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("反序列化回调载荷失败", e);
        }
    }

    private String abbreviateError(final Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getSimpleName();
        }
        if (message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_LENGTH);
    }

    /**
     * 验证回调 URL 安全性。
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
            if (isAllowInternalCallback()) {
                log.debug("允许内网回调已启用，跳过内网地址检查: {}", host);
                return;
            }
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

    private void sendCallback(final String url, final Map<String, Object> data, final String callbackApiKey) {
        validateCallbackUrl(url);

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                doSendCallback(url, data, callbackApiKey);
                return;
            } catch (Exception e) {
                lastException = e;
                log.warn("回调请求失败，第{}次尝试: url={}, error={}", attempt, url, e.getMessage());
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
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

        log.error("回调请求在{}次重试后仍然失败: url={}", MAX_RETRY_ATTEMPTS, url, lastException);
        if (lastException != null) {
            throw new RuntimeException("回调失败: " + lastException.getMessage(), lastException);
        }
    }

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

    private void doSendCallback(final String url, final Map<String, Object> data, final String callbackApiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (callbackApiKey != null && !callbackApiKey.isEmpty()) {
            headers.set("X-Callback-Key", callbackApiKey);
            headers.set("Authorization", "Bearer " + callbackApiKey);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String nonce = UUID.randomUUID().toString().replace("-", "");
            headers.set(CALLBACK_TIMESTAMP_HEADER, timestamp);
            headers.set(CALLBACK_NONCE_HEADER, nonce);
            headers.set(CALLBACK_SIGNATURE_HEADER,
                    signCallback(callbackApiKey, "POST", normalizeCallbackPath(url), timestamp, nonce));
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("回调请求返回非成功状态: " + response.getStatusCode());
        }
        log.debug("回调请求成功: url={}", url);
    }

    private String normalizeCallbackPath(final String url) {
        try {
            String path = new URI(url).getPath();
            if (path == null || path.isBlank()) {
                return "/";
            }
            if (path.startsWith("/api/open/client/")) {
                return path.substring(4);
            }
            return path;
        } catch (Exception e) {
            throw new IllegalArgumentException("回调地址无法解析路径: " + e.getMessage(), e);
        }
    }

    private String signCallback(
            final String secret,
            final String method,
            final String path,
            final String timestamp,
            final String nonce) {
        try {
            String payload = method + "\n" + path + "\n" + timestamp + "\n" + nonce;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("生成回调签名失败", e);
        }
    }

    private String resolveLawFirmCallbackUrl(final ClientMatter matter) {
        Map<String, Object> metadata = parseMatterMetadata(matter);
        Object callbackUrl = metadata.get(CALLBACK_URL_KEY);
        if (callbackUrl instanceof String callbackUrlValue && !callbackUrlValue.isBlank()) {
            return callbackUrlValue.trim();
        }
        return getLawFirmCallbackUrl();
    }

    private String resolveCallbackApiKey(final ClientMatter matter) {
        Object apiKeyIdObj = matter != null ? matter.getSourceApiKeyId() : null;
        if (apiKeyIdObj == null) {
            Map<String, Object> metadata = parseMatterMetadata(matter);
            apiKeyIdObj = metadata.get(SOURCE_API_KEY_ID_KEY);
        }
        if (apiKeyIdObj != null) {
            try {
                Long apiKeyId = Long.parseLong(String.valueOf(apiKeyIdObj));
                ApiKey apiKey = apiKeyMapper.selectById(apiKeyId);
                if (apiKey != null && Boolean.TRUE.equals(apiKey.getEnabled())) {
                    if (apiKey.getApiSecret() != null && !apiKey.getApiSecret().isBlank()) {
                        return apiKey.getApiSecret();
                    }
                    if (apiKey.getApiKey() != null && !apiKey.getApiKey().isBlank()) {
                        return apiKey.getApiKey();
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("项目回调元数据中的 API Key ID 非法: {}", apiKeyIdObj);
            }
        }
        return getCallbackApiKey();
    }

    private Map<String, Object> parseMatterMetadata(final ClientMatter matter) {
        if (matter == null || matter.getMatterData() == null || matter.getMatterData().isBlank()) {
            return java.util.Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(matter.getMatterData(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("解析项目回调元数据失败: matterId={}", matter.getId(), e);
            return java.util.Collections.emptyMap();
        }
    }
}
