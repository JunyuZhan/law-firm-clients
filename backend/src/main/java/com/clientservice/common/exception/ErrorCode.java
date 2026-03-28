package com.clientservice.common.exception;

/**
 * 错误码常量类
 * 
 * <p>统一管理系统中使用的错误码，避免硬编码字符串，提升代码可维护性。</p>
 * 
 * <p>错误码说明：</p>
 * <ul>
 *   <li>400: 客户端请求错误（参数错误、验证失败等）</li>
 *   <li>401: 未授权（认证失败、Token无效等）</li>
 *   <li>403: 禁止访问（权限不足、账户锁定、已过期等）</li>
 *   <li>404: 资源不存在</li>
 *   <li>429: 请求过于频繁（速率限制）</li>
 *   <li>500: 服务器内部错误</li>
 * </ul>
 */
public final class ErrorCode {

    private ErrorCode() {
        // 工具类，禁止实例化
    }

    /** 客户端请求错误 */
    public static final String BAD_REQUEST = "400";

    /** 未授权（认证失败、Token无效等） */
    public static final String UNAUTHORIZED = "401";

    /** 禁止访问（权限不足、账户锁定、已过期等） */
    public static final String FORBIDDEN = "403";

    /** 资源不存在 */
    public static final String NOT_FOUND = "404";

    /** 请求过于频繁（速率限制） */
    public static final String TOO_MANY_REQUESTS = "429";

    /** 服务器内部错误 */
    public static final String INTERNAL_SERVER_ERROR = "500";
}
