package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.CaptchaResponse;
import com.clientservice.application.dto.ChangePasswordRequest;
import com.clientservice.application.dto.LoginRequest;
import com.clientservice.application.dto.LoginResponse;
import com.clientservice.application.dto.UserInfo;
import com.clientservice.application.service.AdminUserService;
import com.clientservice.application.service.CaptchaService;
import com.clientservice.application.service.RequestRateLimitService;
import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.result.Result;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.filter.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员认证控制器
 */
@Slf4j
@Tag(name = "管理员认证", description = "管理员登录、登出、获取用户信息等接口")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminUserService adminUserService;
    private final CaptchaService captchaService;
    private final RequestRateLimitService requestRateLimitService;
    private final com.clientservice.application.service.TokenBlacklistService tokenBlacklistService;
    private final com.clientservice.application.service.CsrfTokenService csrfTokenService;

    /**
     * 获取验证码
     *
     * @param captchaId 验证码ID（可选，如果提供则刷新该验证码，否则生成新的）
     * @return 验证码响应（包含验证码ID和图片）
     */
    @Operation(summary = "获取验证码", description = "获取图形验证码，用于登录时验证")
    @GetMapping("/captcha")
    public Result<CaptchaResponse> getCaptcha(@RequestParam(required = false) String captchaId) {
        // 如果没有提供captchaId，生成新的
        if (captchaId == null || captchaId.isEmpty()) {
            captchaId = UUID.randomUUID().toString();
        }

        String captchaImage = captchaService.generateCaptchaImage(captchaId);

        CaptchaResponse response = CaptchaResponse.builder()
                .captchaId(captchaId)
                .captchaImage(captchaImage)
                .build();

        return Result.success(response);
    }

    /**
     * 登录接口
     *
     * @param request 登录请求
     * @param httpRequest HTTP请求（用于获取IP和User-Agent）
     * @return 登录响应（包含Token和用户信息）
     */
    @Operation(summary = "管理员登录", description = "使用用户名、密码和验证码登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // 1. 检查请求速率限制（防止DDoS）
        try {
            requestRateLimitService.checkRateLimit(ipAddress, "/api/admin/auth/login");
        } catch (BusinessException e) {
            // 速率限制异常直接抛出
            throw e;
        }

        // 2. 验证验证码
        if (request.getCaptchaId() == null || request.getCaptchaId().isEmpty() ||
            request.getCaptchaText() == null || request.getCaptchaText().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码不能为空");
        }

        if (!captchaService.validateCaptcha(request.getCaptchaId(), request.getCaptchaText())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误，请重新获取");
        }

        // 3. 执行登录
        String token = adminUserService.login(request.getUsername(), request.getPassword(), ipAddress, userAgent);
        AdminUser user = adminUserService.getUserByUsername(request.getUsername());

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        // 4. 生成 CSRF Token（使用用户ID作为会话ID）
        String csrfToken = csrfTokenService.generateToken(String.valueOf(user.getId()));

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .user(userInfo)
                .csrfToken(csrfToken)
                .build();

        return Result.success(response);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息和 CSRF Token
     */
    @Operation(summary = "获取当前用户信息", description = "从JWT Token中获取当前登录用户信息（过滤器已验证Token）")
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        // 从JWT过滤器获取当前登录用户（过滤器已验证Token）
        AdminUser user = JwtAuthenticationFilter.getCurrentUser();
        if (user == null) {
            return Result.unauthorized("未登录");
        }

        UserInfo userInfo = UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        // 生成或刷新 CSRF Token
        String csrfToken = csrfTokenService.generateToken(String.valueOf(user.getId()));

        Map<String, Object> result = new HashMap<>();
        result.put("user", userInfo);
        result.put("csrfToken", csrfToken);

        return Result.success(result);
    }

    /**
     * 登出接口
     *
     * @param httpRequest HTTP请求（用于获取Token）
     * @return 成功响应
     */
    @Operation(summary = "管理员登出", description = "登出并将Token加入黑名单，使Token立即失效")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest httpRequest) {
        // 从JWT过滤器获取当前登录用户（过滤器已验证Token）
        AdminUser user = JwtAuthenticationFilter.getCurrentUser();
        if (user != null) {
            log.info("管理员登出: username={}", user.getUsername());
        }
        
        // 从请求头提取Token并加入黑名单
        String token = extractToken(httpRequest);
        if (token != null) {
            tokenBlacklistService.addToBlacklist(token);
            log.info("Token已加入黑名单: username={}", user != null ? user.getUsername() : "unknown");
        }
        
        return Result.success();
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 修改密码
     *
     * @param request 修改密码请求
     * @return 成功响应
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码（过滤器已验证Token）")
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        // 从JWT过滤器获取当前登录用户（过滤器已验证Token）
        AdminUser user = JwtAuthenticationFilter.getCurrentUser();
        if (user == null) {
            return Result.unauthorized("未登录");
        }

        adminUserService.changePassword(user.getId(), request.getOldPassword(), request.getNewPassword());

        return Result.success();
    }

    /**
     * 解锁账户（管理员手动解锁）
     *
     * @param userId 用户ID
     * @return 成功响应
     */
    @Operation(summary = "解锁账户", description = "管理员手动解锁被锁定的账户（过滤器已验证Token）")
    @PostMapping("/unlock/{userId}")
    public Result<Void> unlockAccount(
            @Parameter(description = "用户ID", required = true) @PathVariable final Long userId) {
        // 从JWT过滤器获取当前登录用户（过滤器已验证Token）
        AdminUser currentUser = JwtAuthenticationFilter.getCurrentUser();
        if (currentUser == null) {
            return Result.unauthorized("未登录");
        }

        adminUserService.unlockAccount(userId);

        log.info("管理员解锁账户: operator={}, targetUserId={}", currentUser.getUsername(), userId);

        return Result.success();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
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
        return ip != null ? ip : "unknown";
    }
}
