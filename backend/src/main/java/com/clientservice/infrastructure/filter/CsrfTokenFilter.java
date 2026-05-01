package com.clientservice.infrastructure.filter;

import com.clientservice.application.service.CsrfTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CSRF Token 验证过滤器
 * 
 * <p>验证 POST/PUT/DELETE/PATCH 请求的 CSRF Token，防止跨站请求伪造攻击
 */
@Slf4j
@Component
@Order(2) // 在 JWT 过滤器之后执行
@RequiredArgsConstructor
public class CsrfTokenFilter extends OncePerRequestFilter {

    private final CsrfTokenService csrfTokenService;

    /** 需要 CSRF 验证的 HTTP 方法 */
    private static final String[] CSRF_PROTECTED_METHODS = {"POST", "PUT", "DELETE", "PATCH"};

    /** 排除 CSRF 验证的路径 */
    private static final String[] CSRF_EXCLUDED_PATHS = {
        "/api/admin/auth/login",  // 登录接口（登录时生成 CSRF Token）
        "/api/admin/auth/captcha", // 验证码接口
        "/api/admin/api-keys/init", // 初始化接口
        "/api/admin/fix/password" // 开发环境密码修复接口（精确匹配）
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // 只验证需要 CSRF 保护的 HTTP 方法
        if (!needsCsrfProtection(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 排除不需要 CSRF 验证的路径
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查是否是管理后台 API（需要 JWT 认证的接口才需要 CSRF 保护）
        if (!isAdminApi(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取当前登录用户
        com.clientservice.domain.entity.AdminUser user = JwtAuthenticationFilter.getCurrentUser();
        if (user == null) {
            // 如果没有登录用户，跳过 CSRF 验证（JWT 过滤器会处理认证）
            filterChain.doFilter(request, response);
            return;
        }

        // 从请求头获取 CSRF Token
        String csrfToken = request.getHeader("X-CSRF-Token");
        if (csrfToken == null || csrfToken.isEmpty()) {
            log.warn("CSRF Token 缺失: path={}, method={}, username={}", path, method, user.getUsername());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":\"CSRF_TOKEN_MISSING\",\"message\":\"安全令牌缺失，请重新登录\"}");
            return;
        }

        // 从请求头获取 JWT Token 并计算哈希作为 Session ID
        String jwtToken = request.getHeader("Authorization");
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
        } else {
            filterChain.doFilter(request, response);
            return;
        }
        String sessionId = cn.hutool.crypto.digest.DigestUtil.sha256Hex(jwtToken);

        // 验证 CSRF Token
        if (!csrfTokenService.validateToken(sessionId, csrfToken)) {
            log.warn("CSRF Token 验证失败: path={}, method={}, username={}", path, method, user.getUsername());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"code\":\"CSRF_TOKEN_INVALID\",\"message\":\"安全令牌已失效，请重新登录\"}");
            return;
        }

        // 刷新 CSRF Token（延长有效期）
        csrfTokenService.refreshToken(sessionId, csrfToken);

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否需要 CSRF 保护
     */
    private boolean needsCsrfProtection(String method) {
        for (String protectedMethod : CSRF_PROTECTED_METHODS) {
            if (protectedMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是排除的路径
     */
    private boolean isExcludedPath(String path) {
        for (String excludedPath : CSRF_EXCLUDED_PATHS) {
            if (path.equals(excludedPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是管理后台 API
     */
    private boolean isAdminApi(String path) {
        return path.startsWith("/api/admin/") ||
               path.startsWith("/api/matter/") ||
               path.startsWith("/api/notification/");
    }
}
