package com.clientservice.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private String code;

    /**
     * 构造函数
     *
     * @param message 错误消息
     */
    public BusinessException(final String message) {
        super(message);
        this.code = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * 构造函数
     *
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(final String code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数
     *
     * @param message 错误消息
     * @param cause 原因
     */
    public BusinessException(final String message, final Throwable cause) {
        super(message, cause);
        this.code = ErrorCode.INTERNAL_SERVER_ERROR;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public String getCode() {
        return code;
    }
}
