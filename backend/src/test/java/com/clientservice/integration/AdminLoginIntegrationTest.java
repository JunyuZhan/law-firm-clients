package com.clientservice.integration;

import com.clientservice.application.service.AdminUserService;
import com.clientservice.common.util.PasswordUtil;
import com.clientservice.domain.entity.AdminLoginLog;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.clientservice.infrastructure.persistence.mapper.AdminUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理员登录集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("管理员登录集成测试")
class AdminLoginIntegrationTest {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private AdminLoginLogMapper adminLoginLogMapper;

    @Autowired
    private PasswordUtil passwordUtil;

    private String testUsername = "admin";
    private String testPassword = "admin123";

    @BeforeEach
    void setUp() {
        // 确保测试用户存在，如果不存在则创建
        AdminUser user = adminUserMapper.selectByUsername(testUsername);
        if (user == null) {
            // 创建测试用户
            user = new AdminUser();
            user.setUsername(testUsername);
            user.setPasswordHash(passwordUtil.encode(testPassword));
            user.setRealName("系统管理员");
            user.setEmail("admin@example.com");
            user.setEnabled(true);
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            adminUserMapper.insert(user);
        } else {
            // 如果用户已存在，确保密码正确
            String correctHash = passwordUtil.encode(testPassword);
            user.setPasswordHash(correctHash);
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            user.setEnabled(true);
            adminUserMapper.updateById(user);
        }
    }

    @Test
    @DisplayName("完整登录流程：登录 → 获取用户信息 → 访问管理后台API → 登出")
    void fullLoginFlow_ShouldWork() {
        // Step 1: 登录
        String token = adminUserService.login(testUsername, testPassword, "127.0.0.1", "Test-Agent");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Step 2: 验证Token并获取用户信息
        AdminUser user = adminUserService.validateToken(token);
        assertNotNull(user);
        assertEquals(testUsername, user.getUsername());

        // Step 3: 验证登录日志已记录
        List<AdminLoginLog> logs = adminLoginLogMapper.selectList(null);
        assertFalse(logs.isEmpty());
        AdminLoginLog lastLog = logs.get(logs.size() - 1);
        assertTrue(lastLog.getSuccess());
        assertEquals(testUsername, lastLog.getUsername());
    }

    @Test
    @DisplayName("Token验证：有效Token访问API应该成功")
    void validateToken_WithValidToken_ShouldSuccess() {
        // Given
        String token = adminUserService.login(testUsername, testPassword, "127.0.0.1", "Test-Agent");

        // When
        AdminUser user = adminUserService.validateToken(token);

        // Then
        assertNotNull(user);
        assertEquals(testUsername, user.getUsername());
    }

    @Test
    @DisplayName("Token验证：无效Token访问API应该失败")
    void validateToken_WithInvalidToken_ShouldFail() {
        // Given
        String invalidToken = "invalid-token-string";

        // When & Then
        assertThrows(Exception.class, () -> {
            adminUserService.validateToken(invalidToken);
        });
    }

    @Test
    @DisplayName("登录日志记录：成功登录应该记录日志")
    void loginLog_WithSuccessfulLogin_ShouldRecordLog() {
        // Given
        int initialLogCount = adminLoginLogMapper.selectList(null).size();

        // When
        adminUserService.login(testUsername, testPassword, "127.0.0.1", "Test-Agent");

        // Then
        List<AdminLoginLog> logs = adminLoginLogMapper.selectList(null);
        assertEquals(initialLogCount + 1, logs.size());
        AdminLoginLog lastLog = logs.get(logs.size() - 1);
        assertTrue(lastLog.getSuccess());
        assertEquals(testUsername, lastLog.getUsername());
        assertEquals("127.0.0.1", lastLog.getIpAddress());
        assertEquals("Test-Agent", lastLog.getUserAgent());
    }

    @Test
    @DisplayName("登录日志记录：失败登录应该记录日志")
    void loginLog_WithFailedLogin_ShouldRecordLog() {
        // Given
        int initialLogCount = adminLoginLogMapper.selectList(null).size();
        String wrongPassword = "wrongpassword";

        // When
        try {
            adminUserService.login(testUsername, wrongPassword, "127.0.0.1", "Test-Agent");
            fail("应该抛出异常");
        } catch (Exception e) {
            // 预期会抛出异常
        }

        // Then
        List<AdminLoginLog> logs = adminLoginLogMapper.selectList(null);
        assertEquals(initialLogCount + 1, logs.size());
        AdminLoginLog lastLog = logs.get(logs.size() - 1);
        assertFalse(lastLog.getSuccess());
        assertEquals(testUsername, lastLog.getUsername());
        assertNotNull(lastLog.getFailureReason());
    }

    @Test
    @DisplayName("账户锁定机制：连续失败5次后锁定30分钟")
    void accountLock_AfterFiveFailedAttempts_ShouldLockAccount() {
        // Given
        String wrongPassword = "wrongpassword";
        AdminUser user = adminUserMapper.selectByUsername(testUsername);
        assertNotNull(user);

        // When - 连续失败5次
        for (int i = 0; i < 5; i++) {
            try {
                adminUserService.login(testUsername, wrongPassword, "127.0.0.1", "Test-Agent");
                fail("应该抛出异常");
            } catch (Exception e) {
                // 预期会抛出异常
            }
            // 重新查询用户以获取最新的失败次数
            user = adminUserMapper.selectById(user.getId());
        }

        // Then - 账户应该被锁定
        user = adminUserMapper.selectById(user.getId());
        assertNotNull(user.getLockedUntil());
        assertTrue(user.getLockedUntil().isAfter(LocalDateTime.now()));

        // 尝试登录应该失败
        assertThrows(Exception.class, () -> {
            adminUserService.login(testUsername, testPassword, "127.0.0.1", "Test-Agent");
        });
    }

    @Test
    @DisplayName("修改密码流程：成功修改密码")
    void changePassword_WithValidOldPassword_ShouldSuccess() {
        // Given
        AdminUser user = adminUserMapper.selectByUsername(testUsername);
        assertNotNull(user);
        String oldPassword = testPassword;
        String newPassword = "newPassword123";

        // When
        adminUserService.changePassword(user.getId(), oldPassword, newPassword);

        // Then - 使用新密码应该可以登录
        String token = adminUserService.login(testUsername, newPassword, "127.0.0.1", "Test-Agent");
        assertNotNull(token);

        // 恢复原密码（用于后续测试）
        adminUserService.changePassword(user.getId(), newPassword, oldPassword);
    }

    @Test
    @DisplayName("修改密码流程：旧密码错误应该失败")
    void changePassword_WithWrongOldPassword_ShouldFail() {
        // Given
        AdminUser user = adminUserMapper.selectByUsername(testUsername);
        assertNotNull(user);
        String wrongOldPassword = "wrongpassword";
        String newPassword = "newPassword123";

        // When & Then
        assertThrows(Exception.class, () -> {
            adminUserService.changePassword(user.getId(), wrongOldPassword, newPassword);
        });
    }
}
