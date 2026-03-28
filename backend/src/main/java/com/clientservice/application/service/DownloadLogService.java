package com.clientservice.application.service;

import com.clientservice.domain.entity.DownloadLog;
import com.clientservice.infrastructure.persistence.mapper.DownloadLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件下载日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadLogService {

    private final DownloadLogMapper downloadLogMapper;
    private final CallbackService callbackService;

    /**
     * 记录文件下载日志
     *
     * @param matterId 项目ID
     * @param clientId 客户ID
     * @param fileId 文件ID
     * @param fileName 文件名
     * @param accessToken 访问令牌
     * @param request HTTP请求
     */
    @Transactional
    public void recordDownload(
            final String matterId,
            final Long clientId,
            final String fileId,
            final String fileName,
            final String accessToken,
            final HttpServletRequest request) {

        DownloadLog downloadLog = DownloadLog.builder()
                .matterId(matterId)
                .clientId(clientId)
                .fileId(fileId)
                .fileName(fileName)
                .accessToken(accessToken)
                .ipAddress(getClientIpAddress(request))
                .userAgent(request.getHeader("User-Agent"))
                .downloadTime(LocalDateTime.now())
                .build();

        downloadLogMapper.insert(downloadLog);
        log.debug("记录文件下载日志: matterId={}, fileId={}, fileName={}", 
                matterId, fileId, fileName);

        // 异步回调给管理系统
        try {
            callbackService.callbackDownloadLog(downloadLog);
        } catch (Exception e) {
            // 回调失败不影响主流程
            log.warn("下载日志回调失败: matterId={}, fileId={}", matterId, fileId, e);
        }
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return IP地址
     */
    private String getClientIpAddress(final HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果包含多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
