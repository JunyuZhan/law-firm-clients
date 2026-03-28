package com.clientservice.application.service;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.util.JwtUtil;
import com.clientservice.common.util.PasswordUtil;
import com.clientservice.domain.entity.AdminLoginLog;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.clientservice.infrastructure.persistence.mapper.AdminUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

/**
 * 管理员用户服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("管理员用户服务测试")
class AdminUserServiceTest {

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private AdminLoginLogMapper adminLoginLogMapper;

    @Mock
    private PasswordUtil passwordUtil;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginRateLimitService loginRateLimitService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private AdminUserService adminUserService;

    private AdminUser validUser;
    private String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminUserService, "maxFailedLoginCount", 5);
        ReflectionTestUtils.setField(adminUserService, "lockDurationMinutes", 30);
        
        validUser = new AdminUser();
        validUser.setId(1L);
        validUser.setUsername("admin");
        validUser.setPasswordHash(hashedPassword);
        validUser.setRealName("管理员");
        validUser.setEmail("admin@example.com");
        validUser.setEnabled(true);
        validUser.setFailedLoginCount(0);
        validUser.setLockedUntil(null);
    }

    @Nested
    @DisplayName("登录测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功应该返回Token")
        void login_WithValidCredentials_ShouldReturnToken() {
            // Given
            String username = "admin";
            String password = "admin123";
            String ipAddress = "127.0.0.1";
            String userAgent = "Mozilla/5.0";
            String token = "test-jwt-token";

            doNothing().when(loginRateLimitService).checkIpLocked(anyString());
            doNothing().when(loginRateLimitService).clearAttempts(anyString());
            when(adminUserMapper.selectByUsername(username)).thenReturn(validUser);
            when(passwordUtil.matches(password, hashedPassword)).thenReturn(true);
            when(jwtUtil.generateToken(validUser.getId(), username)).thenReturn(token);
            doNothing().when(adminUserMapper).resetFailedLoginCount(anyLong());
            doNothing().when(adminUserMapper).updateLastLoginTime(anyLong(), anyString());
            when(adminLoginLogMapper.insert(any(AdminLoginLog.class))).thenReturn(1);

            // When
            String result = adminUserService.login(username, password, ipAddress, userAgent);

            // Then
            assertNotNull(result);
            assertEquals(token, result);
            verify(adminUserMapper).resetFailedLoginCount(eq(validUser.getId()));
            verify(adminUserMapper).updateLastLoginTime(eq(validUser.getId()), anyString());
            verify(adminLoginLogMapper).insert(argThat(log ->
                    log.getSuccess() && log.getUserId().equals(validUser.getId())));
        }

        @Test
        @DisplayName("用户名不存在应该抛出异常")
        void login_WithNonExistentUsername_ShouldThrowException() {
            // Given
            String username = "nonexistent";
            doNothing().when(loginRateLimitService).checkIpLocked(anyString());
            doNothing().when(loginRateLimitService).recordFailedAttempt(anyString());
            when(adminUserMapper.selectByUsername(username)).thenReturn(null);
            when(adminLoginLogMapper.insert(any(AdminLoginLog.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.login(username, "password", "127.0.0.1", "Mozilla/5.0");
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getCode());
            assertTrue(exception.getMessage().contains("用户名或密码错误"));
            verify(adminLoginLogMapper).insert(argThat(log ->
                    !log.getSuccess() && AdminLoginLog.FAILURE_USERNAME_NOT_FOUND.equals(log.getFailureReason())));
        }

        @Test
        @DisplayName("密码错误应该抛出异常并增加失败次数")
        void login_WithWrongPassword_ShouldThrowExceptionAndIncrementFailedCount() {
            // Given
            String username = "admin";
            String wrongPassword = "wrongpassword";
            doNothing().when(loginRateLimitService).checkIpLocked(anyString());
            doNothing().when(loginRateLimitService).recordFailedAttempt(anyString());
            when(adminUserMapper.selectByUsername(username)).thenReturn(validUser);
            when(passwordUtil.matches(wrongPassword, hashedPassword)).thenReturn(false);
            doNothing().when(adminUserMapper).incrementFailedLoginCount(anyLong());
            when(adminUserMapper.selectById(validUser.getId())).thenReturn(validUser);
            when(adminLoginLogMapper.insert(any(AdminLoginLog.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.login(username, wrongPassword, "127.0.0.1", "Mozilla/5.0");
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getCode());
            assertTrue(exception.getMessage().contains("用户名或密码错误"));
            verify(adminUserMapper).incrementFailedLoginCount(validUser.getId());
            verify(adminLoginLogMapper).insert(argThat(log ->
                    !log.getSuccess() && AdminLoginLog.FAILURE_PASSWORD_ERROR.equals(log.getFailureReason())));
        }

        @Test
        @DisplayName("账户被锁定应该抛出异常")
        void login_WithLockedAccount_ShouldThrowException() {
            // Given
            String username = "admin";
            doNothing().when(loginRateLimitService).checkIpLocked(anyString());
            validUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            when(adminUserMapper.selectByUsername(username)).thenReturn(validUser);
            when(adminLoginLogMapper.insert(any(AdminLoginLog.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.login(username, "password", "127.0.0.1", "Mozilla/5.0");
            });

            assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
            assertTrue(exception.getMessage().contains("账户已被锁定"));
            verify(adminLoginLogMapper).insert(argThat(log ->
                    !log.getSuccess() && AdminLoginLog.FAILURE_ACCOUNT_LOCKED.equals(log.getFailureReason())));
        }

        @Test
        @DisplayName("账户被禁用应该抛出异常")
        void login_WithDisabledAccount_ShouldThrowException() {
            // Given
            String username = "admin";
            doNothing().when(loginRateLimitService).checkIpLocked(anyString());
            validUser.setEnabled(false);
            when(adminUserMapper.selectByUsername(username)).thenReturn(validUser);
            when(adminLoginLogMapper.insert(any(AdminLoginLog.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.login(username, "password", "127.0.0.1", "Mozilla/5.0");
            });

            assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
            assertTrue(exception.getMessage().contains("账户已被禁用"));
            verify(adminLoginLogMapper).insert(argThat(log ->
                    !log.getSuccess() && AdminLoginLog.FAILURE_ACCOUNT_DISABLED.equals(log.getFailureReason())));
        }

        @Test
        @DisplayName("失败5次后应该锁定账户")
        void login_WithFiveFailedAttempts_ShouldLockAccount() {
            // Given
            String username = "admin";
            String wrongPassword = "wrongpassword";
            doNothing().when(loginRateLimitService).checkIpLocked(anyString());
            doNothing().when(loginRateLimitService).recordFailedAttempt(anyString());
            validUser.setFailedLoginCount(4); // 已经失败4次
            when(adminUserMapper.selectByUsername(username)).thenReturn(validUser);
            when(passwordUtil.matches(wrongPassword, hashedPassword)).thenReturn(false);
            doNothing().when(adminUserMapper).incrementFailedLoginCount(anyLong());
            when(adminUserMapper.selectById(validUser.getId())).thenAnswer(invocation -> {
                AdminUser user = new AdminUser();
                user.setId(validUser.getId());
                user.setUsername(validUser.getUsername());
                user.setRealName(validUser.getRealName());
                user.setEmail(validUser.getEmail());
                user.setFailedLoginCount(5); // 失败次数增加到5
                return user;
            });
            doNothing().when(adminUserMapper).lockAccount(anyLong(), any(LocalDateTime.class));
            when(adminLoginLogMapper.insert(any(AdminLoginLog.class))).thenReturn(1);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.login(username, wrongPassword, "127.0.0.1", "Mozilla/5.0");
            });

            assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
            assertTrue(exception.getMessage().contains("账户已锁定30分钟"));
            verify(adminUserMapper).lockAccount(eq(validUser.getId()), any(LocalDateTime.class));
            verify(emailNotificationService).sendSystemAlert(eq("admin@example.com"), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Token验证测试")
    class ValidateTokenTests {

        @Test
        @DisplayName("有效Token应该返回用户信息")
        void validateToken_WithValidToken_ShouldReturnUser() {
            // Given
            String token = "valid-token";
            when(jwtUtil.validateToken(token)).thenReturn(true);
            when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);
            when(adminUserMapper.selectById(1L)).thenReturn(validUser);

            // When
            AdminUser result = adminUserService.validateToken(token);

            // Then
            assertNotNull(result);
            assertEquals(validUser.getId(), result.getId());
            assertEquals(validUser.getUsername(), result.getUsername());
        }

        @Test
        @DisplayName("无效Token应该抛出异常")
        void validateToken_WithInvalidToken_ShouldThrowException() {
            // Given
            String token = "invalid-token";
            when(jwtUtil.validateToken(token)).thenReturn(false);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.validateToken(token);
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getCode());
            assertTrue(exception.getMessage().contains("Token无效或已过期"));
        }

        @Test
        @DisplayName("用户不存在应该抛出异常")
        void validateToken_WithNonExistentUser_ShouldThrowException() {
            // Given
            String token = "valid-token";
            when(jwtUtil.validateToken(token)).thenReturn(true);
            when(jwtUtil.getUserIdFromToken(token)).thenReturn(999L);
            when(adminUserMapper.selectById(999L)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.validateToken(token);
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getCode());
            assertTrue(exception.getMessage().contains("用户不存在"));
        }

        @Test
        @DisplayName("账户被禁用应该抛出异常")
        void validateToken_WithDisabledUser_ShouldThrowException() {
            // Given
            String token = "valid-token";
            validUser.setEnabled(false);
            when(jwtUtil.validateToken(token)).thenReturn(true);
            when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);
            when(adminUserMapper.selectById(1L)).thenReturn(validUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.validateToken(token);
            });

            assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
            assertTrue(exception.getMessage().contains("账户已被禁用"));
        }
    }

    @Nested
    @DisplayName("修改密码测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("修改密码成功")
        void changePassword_WithValidOldPassword_ShouldSuccess() {
            // Given
            Long userId = 1L;
            String oldPassword = "admin123";
            String newPassword = "newPassword123";
            String newPasswordHash = "$2a$10$NewHash";

            when(adminUserMapper.selectById(userId)).thenReturn(validUser);
            when(passwordUtil.matches(oldPassword, hashedPassword)).thenReturn(true);
            when(passwordUtil.isValidPassword(newPassword)).thenReturn(true);
            when(passwordUtil.encode(newPassword)).thenReturn(newPasswordHash);
            when(adminUserMapper.updateById(any(AdminUser.class))).thenReturn(1);

            // When
            adminUserService.changePassword(userId, oldPassword, newPassword);

            // Then
            verify(adminUserMapper).updateById(argThat(user ->
                    user.getPasswordHash().equals(newPasswordHash)));
        }

        @Test
        @DisplayName("旧密码错误应该抛出异常")
        void changePassword_WithWrongOldPassword_ShouldThrowException() {
            // Given
            Long userId = 1L;
            String wrongOldPassword = "wrongpassword";
            String newPassword = "newPassword123";

            when(adminUserMapper.selectById(userId)).thenReturn(validUser);
            when(passwordUtil.matches(wrongOldPassword, hashedPassword)).thenReturn(false);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.changePassword(userId, wrongOldPassword, newPassword);
            });

            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertTrue(exception.getMessage().contains("旧密码错误"));
        }

        @Test
        @DisplayName("新密码强度不符合要求应该抛出异常")
        void changePassword_WithWeakNewPassword_ShouldThrowException() {
            // Given
            Long userId = 1L;
            String oldPassword = "admin123";
            String weakPassword = "123456"; // 太短，不符合要求
            String policyDescription = "密码要求：至少8位，包含字母，包含数字";

            when(adminUserMapper.selectById(userId)).thenReturn(validUser);
            when(passwordUtil.matches(oldPassword, hashedPassword)).thenReturn(true);
            when(passwordUtil.isValidPassword(weakPassword)).thenReturn(false);
            when(passwordUtil.getPasswordPolicyDescription()).thenReturn(policyDescription);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.changePassword(userId, oldPassword, weakPassword);
            });

            assertEquals(ErrorCode.BAD_REQUEST, exception.getCode());
            assertNotNull(exception.getMessage());
            assertTrue(exception.getMessage().contains("至少") || exception.getMessage().contains("密码要求"));
        }
    }

    @Nested
    @DisplayName("账户解锁测试")
    class UnlockAccountTests {

        @Test
        @DisplayName("解锁账户应该成功")
        void unlockAccount_ShouldSuccess() {
            // Given
            Long userId = 1L;
            validUser.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            when(adminUserMapper.selectById(userId)).thenReturn(validUser);
            doNothing().when(adminUserMapper).unlockAccount(userId);

            // When
            assertDoesNotThrow(() -> adminUserService.unlockAccount(userId));

            // Then
            verify(adminUserMapper).unlockAccount(userId);
        }

        @Test
        @DisplayName("解锁不存在的账户应该抛出异常")
        void unlockAccount_WithNonExistentUser_ShouldThrowException() {
            // Given
            Long userId = 999L;
            when(adminUserMapper.selectById(userId)).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                adminUserService.unlockAccount(userId);
            });

            assertEquals(ErrorCode.NOT_FOUND, exception.getCode());
            assertTrue(exception.getMessage().contains("用户不存在"));
        }
    }
}
