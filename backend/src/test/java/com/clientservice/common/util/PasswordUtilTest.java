package com.clientservice.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码工具类测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("密码工具类测试")
class PasswordUtilTest {

    @InjectMocks
    private PasswordUtil passwordUtil;

    @BeforeEach
    void setUp() {
        // 设置密码策略配置（与默认配置一致）
        ReflectionTestUtils.setField(passwordUtil, "minLength", 8);
        ReflectionTestUtils.setField(passwordUtil, "requireLetter", true);
        ReflectionTestUtils.setField(passwordUtil, "requireDigit", true);
        ReflectionTestUtils.setField(passwordUtil, "requireSpecial", false);
    }

    @Nested
    @DisplayName("密码加密测试")
    class EncodeTests {

        @Test
        @DisplayName("加密密码应该成功")
        void encode_ShouldReturnHashedPassword() {
            // Given
            String rawPassword = "admin123";

            // When
            String hashedPassword = passwordUtil.encode(rawPassword);

            // Then
            assertNotNull(hashedPassword);
            assertFalse(hashedPassword.isEmpty());
            assertNotEquals(rawPassword, hashedPassword);
        }

        @Test
        @DisplayName("相同密码加密结果应该不同（BCrypt每次生成不同的salt）")
        void encode_SamePassword_ShouldReturnDifferentHash() {
            // Given
            String rawPassword = "admin123";

            // When
            String hash1 = passwordUtil.encode(rawPassword);
            String hash2 = passwordUtil.encode(rawPassword);

            // Then
            assertNotEquals(hash1, hash2); // BCrypt每次生成不同的hash
        }
    }

    @Nested
    @DisplayName("密码验证测试")
    class MatchesTests {

        @Test
        @DisplayName("验证正确密码应该返回true")
        void matches_WithCorrectPassword_ShouldReturnTrue() {
            // Given
            String rawPassword = "admin123";
            String hashedPassword = passwordUtil.encode(rawPassword);

            // When
            boolean matches = passwordUtil.matches(rawPassword, hashedPassword);

            // Then
            assertTrue(matches);
        }

        @Test
        @DisplayName("验证错误密码应该返回false")
        void matches_WithWrongPassword_ShouldReturnFalse() {
            // Given
            String correctPassword = "admin123";
            String wrongPassword = "wrongpassword";
            String hashedPassword = passwordUtil.encode(correctPassword);

            // When
            boolean matches = passwordUtil.matches(wrongPassword, hashedPassword);

            // Then
            assertFalse(matches);
        }

        @Test
        @DisplayName("验证空密码应该返回false")
        void matches_WithEmptyPassword_ShouldReturnFalse() {
            // Given
            String rawPassword = "admin123";
            String hashedPassword = passwordUtil.encode(rawPassword);

            // When
            boolean matches = passwordUtil.matches("", hashedPassword);

            // Then
            assertFalse(matches);
        }
    }

    @Nested
    @DisplayName("密码强度验证测试")
    class IsValidPasswordTests {

        @Test
        @DisplayName("符合要求的密码应该返回true")
        void isValidPassword_WithValidPassword_ShouldReturnTrue() {
            // Given
            String[] validPasswords = {
                    "admin123",      // 8位，包含字母和数字
                    "password123",   // 11位，包含字母和数字
                    "Admin123",      // 8位，包含大小写字母和数字
                    "12345678a",     // 9位，包含字母和数字
                    "abcdefgh1"      // 9位，包含字母和数字
            };

            for (String password : validPasswords) {
                // When
                boolean isValid = passwordUtil.isValidPassword(password);

                // Then
                assertTrue(isValid, "密码应该有效: " + password);
            }
        }

        @Test
        @DisplayName("不符合要求的密码应该返回false")
        void isValidPassword_WithInvalidPassword_ShouldReturnFalse() {
            // Given
            String[] invalidPasswords = {
                    "1234567",       // 只有7位，太短
                    "abcdefg",       // 只有7位，且只有字母
                    "12345678",      // 8位，但只有数字
                    "abcdefgh",      // 8位，但只有字母
                    "admin",         // 只有5位
                    "",              // 空字符串
                    null             // null
            };

            for (String password : invalidPasswords) {
                // When
                boolean isValid = passwordUtil.isValidPassword(password);

                // Then
                assertFalse(isValid, "密码应该无效: " + password);
            }
        }

        @Test
        @DisplayName("密码必须至少8位")
        void isValidPassword_MustBeAtLeast8Characters() {
            // Given
            String shortPassword = "abc123"; // 只有6位

            // When
            boolean isValid = passwordUtil.isValidPassword(shortPassword);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("密码必须包含字母")
        void isValidPassword_MustContainLetter() {
            // Given
            String onlyDigits = "12345678"; // 只有数字

            // When
            boolean isValid = passwordUtil.isValidPassword(onlyDigits);

            // Then
            assertFalse(isValid);
        }

        @Test
        @DisplayName("密码必须包含数字")
        void isValidPassword_MustContainDigit() {
            // Given
            String onlyLetters = "abcdefgh"; // 只有字母

            // When
            boolean isValid = passwordUtil.isValidPassword(onlyLetters);

            // Then
            assertFalse(isValid);
        }
    }
}
