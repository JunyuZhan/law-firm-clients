package com.clientservice.interfaces.rest;

import com.clientservice.application.service.CaptchaService;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.result.Result;
import com.clientservice.common.util.PasswordUtil;
import com.clientservice.infrastructure.persistence.mapper.AdminUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员密码修复控制器
 * 
 * <p>仅用于开发环境，生产环境不会加载此控制器
 * <p>用于忘记密码或密码被锁定时的紧急修复
 */
@Slf4j
@Profile("dev")  // 仅在开发环境启用
@Tag(name = "管理员密码修复", description = "开发环境专用的密码修复接口（生产环境不可用）")
@RestController
@RequestMapping("/api/admin/fix")
@RequiredArgsConstructor
public class AdminPasswordFixController {

    private final AdminUserMapper adminUserMapper;
    private final PasswordUtil passwordUtil;
    private final CaptchaService captchaService;

    /**
     * 修复管理员密码（仅开发环境）
     * 
     * <p>功能说明：
     * <ul>
     *   <li>重置指定用户的密码</li>
     *   <li>清除登录失败次数</li>
     *   <li>解除账户锁定</li>
     * </ul>
     * 
     * <p>安全措施：
     * <ul>
     *   <li>需要验证码验证（防止滥用）</li>
     *   <li>仅开发环境可用（生产环境自动禁用）</li>
     * </ul>
     * 
     * @param username 用户名（默认：admin）
     * @param password 新密码（默认：admin123）
     * @param captchaId 验证码ID
     * @param captchaText 验证码文本
     * @param httpRequest HTTP请求（用于记录IP）
     * @return 修复结果
     */
    @Operation(summary = "修复管理员密码", description = "开发环境专用的密码修复接口，用于忘记密码或密码被锁定时的紧急修复。需要验证码验证，仅开发环境可用。")
    @PostMapping("/password")
    public Result<String> fixPassword(
            @Parameter(description = "用户名", example = "admin") @RequestParam(defaultValue = "admin") String username,
            @Parameter(description = "新密码", example = "admin123") @RequestParam(defaultValue = "admin123") String password,
            @Parameter(description = "验证码ID", required = true) @RequestParam(required = true) String captchaId,
            @Parameter(description = "验证码文本", required = true) @RequestParam(required = true) String captchaText,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIpAddress(httpRequest);
        log.warn("密码修复请求: username={}, ip={}", username, ipAddress);
        
        // 验证验证码（开发环境也需要验证码，防止滥用）
        if (captchaId == null || captchaId.isEmpty() || 
            captchaText == null || captchaText.isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "验证码不能为空");
        }
        
        if (!captchaService.validateCaptcha(captchaId, captchaText)) {
            return Result.error(ErrorCode.BAD_REQUEST, "验证码错误，请重新获取");
        }
        
        // 查找用户
        var user = adminUserMapper.selectByUsername(username);
        if (user == null) {
            return Result.error(ErrorCode.NOT_FOUND, "用户不存在: " + username);
        }

        // 验证新密码强度
        if (!passwordUtil.isValidPassword(password)) {
            return Result.error(ErrorCode.BAD_REQUEST, "新密码必须至少8位，且包含字母和数字");
        }

        // 生成密码哈希
        String passwordHash = passwordUtil.encode(password);
        
        // 更新密码并重置账户状态
        user.setPasswordHash(passwordHash);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        adminUserMapper.updateById(user);

        log.warn("密码修复成功: username={}, ip={}", username, ipAddress);
        
        // 安全修复：不在响应中返回明文密码，仅返回成功消息
        return Result.success(String.format(
            "密码修复成功！用户名: %s，请使用您设置的新密码登录。", 
            username));
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
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}
