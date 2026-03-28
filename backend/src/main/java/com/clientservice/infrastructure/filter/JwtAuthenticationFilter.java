package com.clientservice.infrastructure.filter;

import com.clientservice.application.service.AdminUserService;
import com.clientservice.domain.entity.AdminUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT认证过滤器 - 拦截管理后台API请求并验证JWT Token
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminUserService adminUserService;
    private final com.clientservice.application.service.TokenBlacklistService tokenBlacklistService;

    /** 用户上下文ThreadLocal */
    private static final ThreadLocal<AdminUser> USER_CONTEXT = new ThreadLocal<>();

    /**
     * 获取当前登录用户
     *
     * @return 当前用户，如果未登录则返回null
     */
    public static AdminUser getCurrentUser() {
        return USER_CONTEXT.get();
    }

    /**
     * 清除用户上下文
     */
    public static void clearUserContext() {
        USER_CONTEXT.remove();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        // 清除上一个请求的用户上下文（确保线程安全）
        clearUserContext();
        
        try {
            String path = request.getRequestURI();
            
            // 处理OPTIONS预检请求（CORS），直接放行
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // 判断是否需要JWT认证的管理后台API
            boolean needsAuth = isAdminApi(path);
            log.debug("JWT过滤器检查: path={}, needsAuth={}", path, needsAuth);
            
            if (needsAuth) {
                // 从请求头提取Token
                String token = extractToken(request);
                log.debug("提取Token: path={}, token存在={}", path, token != null);
                
                if (token != null) {
                    try {
                        // 1. 检查Token是否在黑名单中（登出后失效）
                        if (tokenBlacklistService.isBlacklisted(token)) {
                            log.warn("Token在黑名单中: path={}", path);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"success\":false,\"code\":\"401\",\"message\":\"Token已失效，请重新登录\"}");
                            return;
                        }
                        
                        // 2. 验证Token并获取用户信息
                        AdminUser user = adminUserService.validateToken(token);
                        
                        // 3. 检查密码是否变更（变更后旧Token自动失效）
                        io.jsonwebtoken.Claims claims = adminUserService.getJwtUtil().parseToken(token);
                        if (claims != null && claims.getIssuedAt() != null) {
                            long issuedAt = claims.getIssuedAt().getTime();
                            if (tokenBlacklistService.isTokenInvalidatedByPasswordChange(user.getId(), issuedAt)) {
                                log.warn("密码已变更，Token已失效: userId={}, path={}", user.getId(), path);
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"success\":false,\"code\":\"401\",\"message\":\"密码已修改，请重新登录\"}");
                                return;
                            }
                        }
                        // 设置用户上下文（在Controller执行前设置，确保Controller可以访问）
                        USER_CONTEXT.set(user);
                        log.info("JWT认证成功: username={}, path={}", user.getUsername(), path);
                    } catch (Exception e) {
                        log.error("JWT认证失败: path={}, error={}", path, e.getMessage(), e);
                        // 认证失败，返回401（CORS响应头由CorsFilter添加）
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"success\":false,\"code\":\"401\",\"message\":\"Token无效或已过期\"}");
                        return;
                    }
                } else {
                    // 未提供Token，返回401（CORS响应头由CorsFilter添加）
                    String authHeader = request.getHeader("Authorization");
                    log.warn("未提供JWT Token: path={}, Authorization header存在={}", path, authHeader != null && !authHeader.isEmpty());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"code\":\"401\",\"message\":\"未提供有效的认证Token\"}");
                    return;
                }
            } else {
                log.debug("路径不需要JWT认证: path={}", path);
            }
            
            // 执行后续过滤器（包括Controller）
            // 注意：filterChain.doFilter()会等待所有后续处理完成才返回
            filterChain.doFilter(request, response);
        } finally {
            // 请求处理完成后清除用户上下文
            // 注意：这里清除是安全的，因为filterChain.doFilter()会等待Controller执行完成
            clearUserContext();
        }
    }

    /**
     * 判断是否是管理后台API（需要JWT认证）
     */
    private boolean isAdminApi(String path) {
        // 排除登录接口、验证码接口和初始化接口
        if (path.equals("/api/admin/auth/login") || 
            path.equals("/api/admin/auth/captcha") ||  // 验证码接口，登录前调用，不需要Token
            path.equals("/api/admin/api-keys/init") ||
            path.equals("/api/admin/fix/password")) {  // 开发环境密码修复（精确匹配，防止路径绕过）
            return false;
        }
        
        // 函件验证公开接口（无需JWT认证，使用API Key认证或完全公开）
        if (path.equals("/letter/verification/verify") ||
            path.startsWith("/letter/verification/info/")) {
            log.debug("函件验证公开接口，跳过JWT认证: path={}", path);
            return false;
        }
        
        // 管理后台API路径
        // 注意：request.getRequestURI() 不包含查询参数，所以可以安全使用 startsWith
        if (path.startsWith("/api/admin/")) {
            return true;
        }
        
        // Matter管理接口（管理后台使用）
        if (path.equals("/api/matter/list") || 
            path.startsWith("/api/matter/detail/") ||
            path.equals("/api/matter/revoke")) {
            return true;
        }
        
        // 通知管理接口（管理后台使用）
        if (path.equals("/api/notification/history") ||
            path.equals("/api/notification/send") ||
            path.equals("/api/notification/statistics")) {
            log.debug("匹配到通知管理接口: path={}", path);
            return true;
        }
        
        return false;
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
