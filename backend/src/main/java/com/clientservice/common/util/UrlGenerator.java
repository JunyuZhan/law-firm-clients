package com.clientservice.common.util;

import com.clientservice.application.service.SysConfigService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * URL生成器
 * 支持从系统配置（sys_config表）动态读取 baseUrl，优先级高于配置文件
 * 如果配置为空或 "auto"，且存在请求上下文，则自动从请求中获取 baseUrl
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UrlGenerator {

    private static final Pattern SAFE_HOST_PATTERN = Pattern.compile("^[a-zA-Z0-9.-]+(?::\\d{1,5})?$");

    private final SysConfigService sysConfigService;

    /** 系统基础URL（配置文件中的默认值） */
    @Value("${client-service.system.base-url:http://localhost:8081}")
    private String defaultBaseUrl;

    /**
     * 是否允许从请求头推导外部访问域名。
     *
     * <p>默认关闭，避免请求头投毒导致生成恶意外链。
     */
    @Value("${client-service.system.allow-request-base-url:false}")
    private boolean allowRequestDerivedBaseUrl;

    /**
     * 获取系统基础URL（优先从系统配置读取）
     * 如果配置为空或 "auto"，且存在请求上下文，则从请求中动态获取
     *
     * @param request HTTP请求（可选，如果为null则尝试从RequestContextHolder获取）
     * @return 系统基础URL
     */
    private String getBaseUrl(HttpServletRequest request) {
        // 尝试从系统配置读取
        String configValue = sysConfigService.getConfigValue("system.base-url", defaultBaseUrl);
        
        // 如果配置为空、null 或 "auto"，尝试从请求上下文动态获取
        if ((configValue == null || configValue.isEmpty() || "auto".equalsIgnoreCase(configValue.trim()))
                && (request != null || RequestContextHolder.getRequestAttributes() != null)) {
            if (!allowRequestDerivedBaseUrl) {
                log.warn("system.base-url 未显式配置，且已禁用从请求头推导 baseUrl，回退到默认配置");
                return normalizeBaseUrl(defaultBaseUrl);
            }
            
            HttpServletRequest httpRequest = request;
            if (httpRequest == null) {
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    httpRequest = attributes.getRequest();
                }
            }
            
            if (httpRequest != null) {
                String dynamicBaseUrl = buildBaseUrlFromRequest(httpRequest);
                if (dynamicBaseUrl != null) {
                    log.debug("从请求上下文动态获取 baseUrl: {}", dynamicBaseUrl);
                    return dynamicBaseUrl;
                }
            }

            return normalizeBaseUrl(defaultBaseUrl);
        }
        
        return normalizeBaseUrl(configValue != null ? configValue : defaultBaseUrl);
    }

    /**
     * 从HTTP请求构建基础URL
     *
     * @param request HTTP请求
     * @return 基础URL，如果无法构建则返回null
     */
    private String buildBaseUrlFromRequest(HttpServletRequest request) {
        try {
            // 获取协议（http 或 https）
            String scheme = request.getScheme();
            
            // 获取主机名和端口
            String host = request.getHeader("Host");
            if (host == null || host.isEmpty()) {
                host = request.getServerName();
                int port = request.getServerPort();
                if (port != 80 && port != 443) {
                    host = host + ":" + port;
                }
            }
            
            // 获取 X-Forwarded-Proto（反向代理场景）
            String forwardedProto = request.getHeader("X-Forwarded-Proto");
            if (forwardedProto != null && !forwardedProto.isEmpty()) {
                if (!isSafeScheme(forwardedProto)) {
                    log.warn("检测到不安全的 X-Forwarded-Proto: {}", forwardedProto);
                    return null;
                }
                scheme = forwardedProto;
            }
            
            // 获取 X-Forwarded-Host（反向代理场景）
            String forwardedHost = request.getHeader("X-Forwarded-Host");
            if (forwardedHost != null && !forwardedHost.isEmpty()) {
                if (!isSafeHost(forwardedHost)) {
                    log.warn("检测到不安全的 X-Forwarded-Host: {}", forwardedHost);
                    return null;
                }
                host = forwardedHost;
            }
            
            if (!isSafeScheme(scheme) || !isSafeHost(host)) {
                log.warn("检测到不安全的 baseUrl 请求头: scheme={}, host={}", scheme, host);
                return null;
            }
            
            return scheme + "://" + host;
        } catch (Exception e) {
            log.warn("从请求构建 baseUrl 失败", e);
            return null;
        }
    }

    /**
     * 生成客户访问链接
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @return 访问链接
     */
    public String generateAccessUrl(final String matterId, final String token) {
        return generateAccessUrl(matterId, token, null);
    }

    /**
     * 生成客户访问链接（支持从请求上下文动态获取 baseUrl）
     *
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param request HTTP请求（可选）
     * @return 访问链接
     */
    public String generateAccessUrl(final String matterId, final String token, HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        return String.format("%s/portal/matter/%s?token=%s", baseUrl, matterId, token);
    }

    /**
     * 生成文件下载URL
     *
     * @param fileId 文件ID
     * @param matterId 项目ID
     * @param token 访问令牌
     * @return 文件下载URL
     */
    public String generateFileDownloadUrl(final String fileId, final String matterId, final String token) {
        return generateFileDownloadUrl(fileId, matterId, token, null);
    }

    /**
     * 生成文件下载URL（支持从请求上下文动态获取 baseUrl）
     *
     * @param fileId 文件ID
     * @param matterId 项目ID
     * @param token 访问令牌
     * @param request HTTP请求（可选）
     * @return 文件下载URL
     */
    public String generateFileDownloadUrl(final String fileId, final String matterId, final String token, HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        return String.format("%s/api/client/files/%s/download?matterId=%s&token=%s", 
                baseUrl, fileId, matterId, token);
    }

    /**
     * 设置基础URL（用于测试）
     *
     * @param baseUrl 基础URL
     */
    public void setBaseUrl(final String baseUrl) {
        this.defaultBaseUrl = baseUrl;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return null;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private boolean isSafeScheme(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private boolean isSafeHost(String host) {
        return host != null
                && !host.isBlank()
                && !host.contains("/")
                && !host.contains("\\")
                && !host.contains(",")
                && !host.contains(" ")
                && SAFE_HOST_PATTERN.matcher(host).matches();
    }
}
