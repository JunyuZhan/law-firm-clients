package com.clientservice.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenGenerator 单元测试
 */
@DisplayName("TokenGenerator 单元测试")
class TokenGeneratorTest {

    @Nested
    @DisplayName("生成访问令牌测试")
    class GenerateTokenTests {

        @Test
        @DisplayName("生成32位访问令牌")
        void generateToken_ShouldReturn32Characters() {
            // When
            String token = TokenGenerator.generateToken();

            // Then
            assertNotNull(token);
            assertEquals(32, token.length());
            assertTrue(token.matches("[a-zA-Z0-9]{32}"));
        }

        @Test
        @DisplayName("生成指定长度的令牌")
        void generateToken_WithLength_ShouldReturnCorrectLength() {
            // When
            String token = TokenGenerator.generateToken(16);

            // Then
            assertNotNull(token);
            assertEquals(16, token.length());
            assertTrue(token.matches("[a-zA-Z0-9]{16}"));
        }

        @Test
        @DisplayName("多次生成令牌应该不同")
        void generateToken_MultipleTimes_ShouldBeDifferent() {
            // When
            String token1 = TokenGenerator.generateToken();
            String token2 = TokenGenerator.generateToken();

            // Then
            assertNotEquals(token1, token2);
        }
    }

    @Nested
    @DisplayName("生成项目ID测试")
    class GenerateIdTests {

        @Test
        @DisplayName("生成的项目ID格式正确")
        void generateId_ShouldReturnCorrectFormat() throws InterruptedException {
            // When
            String matterId = TokenGenerator.generateId();

            // Then
            assertNotNull(matterId);
            assertTrue(matterId.startsWith("CS"));
            assertEquals(21, matterId.length()); // CS(2) + 时间戳(13) + 随机数(6) = 21
            assertTrue(matterId.matches("CS\\d{19}"));
        }

        @Test
        @DisplayName("多次生成项目ID应该不同")
        void generateId_MultipleTimes_ShouldBeDifferent() throws InterruptedException {
            // When
            String id1 = TokenGenerator.generateId();
            Thread.sleep(1); // 确保时间戳不同
            String id2 = TokenGenerator.generateId();

            // Then
            assertNotEquals(id1, id2);
        }
    }
}
