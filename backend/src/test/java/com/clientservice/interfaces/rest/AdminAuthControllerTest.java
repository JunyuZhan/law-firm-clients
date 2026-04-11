package com.clientservice.interfaces.rest;

import com.clientservice.application.dto.ChangePasswordRequest;
import com.clientservice.application.dto.LoginRequest;
import com.clientservice.application.service.AdminAuthorizationService;
import com.clientservice.application.service.AdminUserService;
import com.clientservice.application.service.CaptchaService;
import com.clientservice.application.service.CsrfTokenService;
import com.clientservice.application.service.RequestRateLimitService;
import com.clientservice.application.service.TokenBlacklistService;
import com.clientservice.domain.entity.AdminUser;
import com.clientservice.infrastructure.config.GlobalExceptionHandler;
import com.clientservice.infrastructure.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminAuthController 单元测试")
class AdminAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminUserService adminUserService;

    @Mock
    private AdminAuthorizationService adminAuthorizationService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private RequestRateLimitService requestRateLimitService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private CsrfTokenService csrfTokenService;

    @InjectMocks
    private AdminAuthController adminAuthController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockedStatic<JwtAuthenticationFilter> jwtAuthenticationFilterMockedStatic;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminAuthController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        // Mock 静态方法
        jwtAuthenticationFilterMockedStatic = mockStatic(JwtAuthenticationFilter.class);
    }

    @AfterEach
    void tearDown() {
        // 关闭静态 Mock
        if (jwtAuthenticationFilterMockedStatic != null) {
            jwtAuthenticationFilterMockedStatic.close();
        }
    }

    @Nested
    @DisplayName("验证码接口测试")
    class CaptchaTests {

        @Test
        @DisplayName("获取验证码成功")
        void getCaptcha_ShouldReturnSuccess() throws Exception {
            // Given
            String captchaImage = "base64image";
            when(captchaService.generateCaptchaImage(anyString())).thenReturn(captchaImage);

            // When & Then
            mockMvc.perform(get("/api/admin/auth/captcha"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.captchaId").exists())
                    .andExpect(jsonPath("$.data.captchaImage").value(captchaImage));
        }

        @Test
        @DisplayName("刷新验证码成功")
        void refreshCaptcha_ShouldReturnSuccess() throws Exception {
            // Given
            String captchaId = "test-uuid";
            String captchaImage = "base64image";
            when(captchaService.generateCaptchaImage(captchaId)).thenReturn(captchaImage);

            // When & Then
            mockMvc.perform(get("/api/admin/auth/captcha").param("captchaId", captchaId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.captchaId").value(captchaId))
                    .andExpect(jsonPath("$.data.captchaImage").value(captchaImage));
        }
    }

    @Nested
    @DisplayName("登录接口测试")
    class LoginTests {

        @Test
        @DisplayName("登录成功")
        void login_ShouldReturnSuccess() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("admin");
            loginRequest.setPassword("password");
            loginRequest.setCaptchaId("captchaId");
            loginRequest.setCaptchaText("1234");

            AdminUser user = new AdminUser();
            user.setId(1L);
            user.setUsername("admin");
            user.setRealName("Admin");
            user.setEmail("admin@example.com");

            when(captchaService.validateCaptcha(anyString(), anyString())).thenReturn(true);
            when(adminUserService.login(anyString(), anyString(), anyString(), any())).thenReturn("jwt-token");
            when(adminUserService.getUserByUsername("admin")).thenReturn(user);
            when(csrfTokenService.generateToken(anyString())).thenReturn("csrf-token");
            when(adminAuthorizationService.isSuperAdmin(user)).thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.token").value("jwt-token"))
                    .andExpect(jsonPath("$.data.user.username").value("admin"))
                    .andExpect(jsonPath("$.data.user.superAdmin").value(true))
                    .andExpect(jsonPath("$.data.csrfToken").value("csrf-token"));
            
            verify(requestRateLimitService).checkRateLimit(anyString(), anyString());
        }

        @Test
        @DisplayName("验证码错误应该失败")
        void login_WithInvalidCaptcha_ShouldFail() throws Exception {
            // Given
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("admin");
            loginRequest.setPassword("password");
            loginRequest.setCaptchaId("captchaId");
            loginRequest.setCaptchaText("1234");

            when(captchaService.validateCaptcha(anyString(), anyString())).thenReturn(false);

            // When & Then
            mockMvc.perform(post("/api/admin/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isBadRequest()) // GlobalExceptionHandler maps BusinessException(400) to 400
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("验证码错误，请重新获取"));
            
            verify(adminUserService, never()).login(anyString(), anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("获取当前用户信息测试")
    class GetCurrentUserTests {

        @Test
        @DisplayName("获取当前用户信息成功")
        void getCurrentUser_ShouldReturnSuccess() throws Exception {
            // Given
            AdminUser user = new AdminUser();
            user.setId(1L);
            user.setUsername("admin");
            user.setRealName("Admin");
            user.setEmail("admin@example.com");

            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(user);
            when(csrfTokenService.generateToken(anyString())).thenReturn("csrf-token");
            when(adminAuthorizationService.isSuperAdmin(user)).thenReturn(true);

            // When & Then
            mockMvc.perform(get("/api/admin/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.user.username").value("admin"))
                    .andExpect(jsonPath("$.data.user.superAdmin").value(true))
                    .andExpect(jsonPath("$.data.csrfToken").value("csrf-token"));
        }

        @Test
        @DisplayName("未登录时应该返回401")
        void getCurrentUser_WithoutLogin_ShouldReturnUnauthorized() throws Exception {
            // Given
            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/api/admin/auth/me"))
                    .andExpect(status().isOk()) // Result.unauthorized returns 200 OK with 401 code in body
                    .andExpect(jsonPath("$.code").value("401"))
                    .andExpect(jsonPath("$.message").value("未登录"));
        }
    }

    @Nested
    @DisplayName("登出测试")
    class LogoutTests {

        @Test
        @DisplayName("登出成功")
        void logout_ShouldReturnSuccess() throws Exception {
            // Given
            AdminUser user = new AdminUser();
            user.setUsername("admin");
            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(user);

            // When & Then
            mockMvc.perform(post("/api/admin/auth/logout")
                    .header("Authorization", "Bearer test-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(tokenBlacklistService).addToBlacklist("test-token");
        }
    }

    @Nested
    @DisplayName("修改密码测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("修改密码成功")
        void changePassword_ShouldReturnSuccess() throws Exception {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("oldPass");
            request.setNewPassword("newPass");

            AdminUser user = new AdminUser();
            user.setId(1L);
            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(user);

            // When & Then
            mockMvc.perform(post("/api/admin/auth/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(adminUserService).changePassword(eq(1L), eq("oldPass"), eq("newPass"));
        }

        @Test
        @DisplayName("未登录修改密码应该失败")
        void changePassword_WithoutLogin_ShouldFail() throws Exception {
            // Given
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword("oldPass");
            request.setNewPassword("newPass");

            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(null);

            // When & Then
            mockMvc.perform(post("/api/admin/auth/change-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("401"));
            
            verify(adminUserService, never()).changePassword(any(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("解锁账户测试")
    class UnlockAccountTests {

        @Test
        @DisplayName("解锁账户成功")
        void unlockAccount_ShouldReturnSuccess() throws Exception {
            // Given
            AdminUser user = new AdminUser();
            user.setUsername("admin");
            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(user);
            doNothing().when(adminAuthorizationService).requireSuperAdmin();

            // When & Then
            mockMvc.perform(post("/api/admin/auth/unlock/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(adminUserService).unlockAccount(2L);
        }

        @Test
        @DisplayName("非超级管理员解锁账户应返回403")
        void unlockAccount_ShouldReturnForbidden_WhenNotSuperAdmin() throws Exception {
            AdminUser user = new AdminUser();
            user.setUsername("operator");
            jwtAuthenticationFilterMockedStatic.when(JwtAuthenticationFilter::getCurrentUser).thenReturn(user);
            doThrow(new com.clientservice.common.exception.BusinessException("403", "仅超级管理员可执行该操作"))
                    .when(adminAuthorizationService).requireSuperAdmin();

            mockMvc.perform(post("/api/admin/auth/unlock/2"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("403"))
                    .andExpect(jsonPath("$.message").value("仅超级管理员可执行该操作"));

            verify(adminUserService, never()).unlockAccount(any());
        }
    }
}
