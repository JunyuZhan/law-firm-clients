package com.clientservice.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result 单元测试
 */
@DisplayName("Result 单元测试")
class ResultTest {

    @Nested
    @DisplayName("成功响应测试")
    class SuccessTests {

        @Test
        @DisplayName("成功响应应该包含数据")
        void success_ShouldContainData() {
            // When
            Result<String> result = Result.success("test data");

            // Then
            assertTrue(result.isSuccess());
            assertEquals("200", result.getCode());
            assertEquals("test data", result.getData());
            assertNotNull(result.getMessage());
        }

        @Test
        @DisplayName("成功响应可以没有数据")
        void success_WithoutData_ShouldBeSuccess() {
            // When
            Result<Void> result = Result.success();

            // Then
            assertTrue(result.isSuccess());
            assertEquals("200", result.getCode());
            assertNull(result.getData());
        }
    }

    @Nested
    @DisplayName("失败响应测试")
    class FailureTests {

        @Test
        @DisplayName("失败响应应该包含错误信息")
        void failure_ShouldContainErrorMessage() {
            // When
            Result<String> result = Result.error("400", "请求参数错误");

            // Then
            assertFalse(result.isSuccess());
            assertEquals(400, Integer.parseInt(result.getCode()));
            assertEquals("请求参数错误", result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("禁止访问响应")
        void forbidden_ShouldReturn403() {
            // When
            Result<String> result = Result.forbidden("访问被拒绝");

            // Then
            assertFalse(result.isSuccess());
            assertEquals(403, Integer.parseInt(result.getCode()));
            assertEquals("访问被拒绝", result.getMessage());
        }

        @Test
        @DisplayName("未找到响应")
        void notFound_ShouldReturn404() {
            // When
            Result<String> result = Result.notFound("资源不存在");

            // Then
            assertFalse(result.isSuccess());
            assertEquals(404, Integer.parseInt(result.getCode()));
            assertEquals("资源不存在", result.getMessage());
        }
    }
}
