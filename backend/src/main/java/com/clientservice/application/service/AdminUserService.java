package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.util.JwtUtil;
import com.clientservice.common.util.PasswordUtil;
import com.clientservice.domain.entity.AdminLoginLog;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.clientservice.infrastructure.persistence.mapper.AdminUserMapper;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final AdminLoginLogMapper adminLoginLogMapper;
    private final PasswordUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final LoginRateLimitService loginRateLimitService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailNotificationService emailNotificationService;

    /** 获取 JwtUtil（供过滤器解析 Token 签发时间） */
    public JwtUtil getJwtUtil() {
        return jwtUtil;
    }

    /** 最大登录失败次数（可配置） */
    @Value("${client-service.security.max-failed-login-count:5}")
    private int maxFailedLoginCount;

    /** 账户锁定时间（分钟，可配置） */
    @Value("${client-service.security.lock-duration-minutes:30}")
    private int lockDurationMinutes;

    /**
     * 检查系统是否处于未初始化状态
     */
    public boolean needsInit() {
        Integer adminCount = adminUserMapper.countAll();
        return adminCount == null || adminCount == 0;
    }

    /**
     * 初始化管理员账号
     */
    @Transactional
    public void initSetup(String password) {
        if (!needsInit()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统已初始化，无需重复设置");
        }

        if (!passwordUtil.isValidPassword(password)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, passwordUtil.getPasswordPolicyDescription());
        }

        AdminUser adminUser = AdminUser.builder()
                .username("admin")
                .passwordHash(passwordUtil.encode(password))
                .realName("系统管理员")
                .email("archive-admin@localhost")
                .enabled(true)
                .failedLoginCount(0)
                .lockedUntil(null)
                .build();

        adminUserMapper.insert(adminUser);
        log.info("系统初始化完成：成功创建默认管理员账号 (admin)");
    }

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return JWT Token
     */
    @Transactional
    public String login(String username, String password, String ipAddress, String userAgent) {
        // 首先检查IP是否被锁定（防止撞库攻击）
        loginRateLimitService.checkIpLocked(ipAddress);

        AdminUser user = adminUserMapper.selectByUsername(username);

        // 记录登录日志（失败）
        AdminLoginLog loginLog = AdminLoginLog.builder()
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .success(false)
                .build();

        // 用户不存在（执行一次空 BCrypt 验证以统一响应时间，缓解时序攻击）
        if (user == null) {
            passwordUtil.matches(password, "$2a$10$dummyHashToPreventTimingAttack000000000000000000000");
            loginLog.setFailureReason(AdminLoginLog.FAILURE_USERNAME_NOT_FOUND);
            adminLoginLogMapper.insert(loginLog);
            // 记录IP登录失败尝试
            loginRateLimitService.recordFailedAttempt(ipAddress);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 检查账户是否被锁定
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            loginLog.setUserId(user.getId());
            loginLog.setFailureReason(AdminLoginLog.FAILURE_ACCOUNT_LOCKED);
            adminLoginLogMapper.insert(loginLog);
            throw new BusinessException(ErrorCode.FORBIDDEN, "账户已被锁定，请稍后再试");
        }

        // 检查账户是否启用
        if (user.getEnabled() == null || !user.getEnabled()) {
            loginLog.setUserId(user.getId());
            loginLog.setFailureReason(AdminLoginLog.FAILURE_ACCOUNT_DISABLED);
            adminLoginLogMapper.insert(loginLog);
            throw new BusinessException(ErrorCode.FORBIDDEN, "账户已被禁用");
        }

        // 验证密码
        if (!passwordUtil.matches(password, user.getPasswordHash())) {
            // 增加失败次数
            adminUserMapper.incrementFailedLoginCount(user.getId());
            // 重新查询用户以获取最新的失败次数
            user = adminUserMapper.selectById(user.getId());
            int failedCount = user.getFailedLoginCount() != null ? user.getFailedLoginCount() : 0;

            // 如果失败次数达到上限，锁定账户
            if (failedCount >= maxFailedLoginCount) {
                LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
                adminUserMapper.lockAccount(user.getId(), lockedUntil);
                loginLog.setFailureReason(AdminLoginLog.FAILURE_ACCOUNT_LOCKED);
                adminLoginLogMapper.insert(loginLog);
                // 记录IP登录失败尝试
                loginRateLimitService.recordFailedAttempt(ipAddress);
                
                // 发送登录失败告警通知（异步）
                sendLoginFailureAlert(user, failedCount, ipAddress);
                
                throw new BusinessException(ErrorCode.FORBIDDEN, 
                    String.format("登录失败次数过多，账户已锁定%d分钟", lockDurationMinutes));
            }

            loginLog.setUserId(user.getId());
            loginLog.setFailureReason(AdminLoginLog.FAILURE_PASSWORD_ERROR);
            adminLoginLogMapper.insert(loginLog);
            // 记录IP登录失败尝试
            loginRateLimitService.recordFailedAttempt(ipAddress);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 登录成功：重置失败次数、清除锁定、更新最后登录时间和IP
        adminUserMapper.resetFailedLoginCount(user.getId());
        adminUserMapper.updateLastLoginTime(user.getId(), ipAddress);

        // 清除IP登录尝试记录（登录成功）
        loginRateLimitService.clearAttempts(ipAddress);

        // 记录成功登录日志
        loginLog.setUserId(user.getId());
        loginLog.setSuccess(true);
        adminLoginLogMapper.insert(loginLog);

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("管理员登录成功: username={}, userId={}", username, user.getId());

        return token;
    }

    /**
     * 验证Token并返回用户信息
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public AdminUser validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token无效或已过期");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token格式错误");
        }

        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }

        if (user.getEnabled() == null || !user.getEnabled()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账户已被禁用");
        }

        return user;
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 验证旧密码
        if (!passwordUtil.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码错误");
        }

        // 验证新密码强度
        if (!passwordUtil.isValidPassword(newPassword)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, passwordUtil.getPasswordPolicyDescription());
        }

        // 加密新密码并更新
        String newPasswordHash = passwordUtil.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        adminUserMapper.updateById(user);

        // 使该用户修改密码之前签发的所有 Token 失效
        tokenBlacklistService.invalidateUserTokens(userId);

        log.info("管理员修改密码成功: userId={}, username={}", userId, user.getUsername());
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public AdminUser getUserById(Long id) {
        return adminUserMapper.selectById(id);
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    public AdminUser getUserByUsername(String username) {
        return adminUserMapper.selectByUsername(username);
    }

    /**
     * 解锁账户（管理员手动解锁）
     *
     * @param userId 用户ID
     */
    @Transactional
    public void unlockAccount(Long userId) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }

        // 解锁账户（清除锁定时间和失败次数）
        adminUserMapper.unlockAccount(userId);

        log.info("管理员手动解锁账户: userId={}, username={}", userId, user.getUsername());
    }

    /**
     * 发送登录失败告警通知（异步）
     *
     * @param user 用户
     * @param failedCount 失败次数
     * @param ipAddress IP地址
     */
    private void sendLoginFailureAlert(AdminUser user, int failedCount, String ipAddress) {
        // 如果失败次数达到阈值，发送告警通知
        if (failedCount >= maxFailedLoginCount) {
            log.warn("账户登录失败次数过多，触发告警: userId={}, username={}, failedCount={}, ip={}", 
                    user.getId(), user.getUsername(), failedCount, ipAddress);
            
            // 发送告警邮件
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "【安全告警】管理员账户已被锁定";
                String content = String.format(
                    "尊敬的 %s：<br/><br/>" +
                    "您的账户（用户名：%s）因多次登录失败已被锁定。<br/>" +
                    "登录IP：%s<br/>" +
                    "锁定时间：%d分钟<br/>" +
                    "如果这不是您本人的操作，请立即联系系统管理员。",
                    user.getRealName() != null ? user.getRealName() : user.getUsername(),
                    user.getUsername(),
                    ipAddress,
                    lockDurationMinutes
                );
                
                // 异步发送或直接发送（Service中已处理异常）
                try {
                    emailNotificationService.sendSystemAlert(user.getEmail(), subject, content);
                } catch (Exception e) {
                    log.error("发送登录失败告警邮件异常", e);
                }
            }
        }
    }
}
