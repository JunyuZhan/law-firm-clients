package com.clientservice.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.clientservice.common.exception.ErrorCode.*;

/**
 * BusinessException 单元测试
 */
@DisplayName("BusinessException 单元测试")
class BusinessExceptionTest {

    @Test
    @DisplayName("创建业务异常应该包含错误码和消息")
    void createException_ShouldContainCodeAndMessage() {
        // When
        BusinessException exception = new BusinessException(BAD_REQUEST, "请求参数错误");

        // Then
        assertEquals(BAD_REQUEST, exception.getCode());
        assertEquals("请求参数错误", exception.getMessage());
    }

    @Test
    @DisplayName("只提供消息应该使用默认错误码")
    void createException_WithMessageOnly_ShouldUseDefaultCode() {
        // When
        BusinessException exception = new BusinessException("业务处理失败");

        // Then
        assertNotNull(exception.getCode());
        assertEquals("业务处理失败", exception.getMessage());
    }

    @Test
    @DisplayName("异常应该继承RuntimeException")
    void exception_ShouldExtendRuntimeException() {
        // When
        BusinessException exception = new BusinessException(BAD_REQUEST, "错误");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }
}
