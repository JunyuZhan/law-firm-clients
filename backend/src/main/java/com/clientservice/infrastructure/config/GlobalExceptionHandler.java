package com.clientservice.infrastructure.config;

import com.clientservice.common.exception.BusinessException;
import com.clientservice.common.exception.ErrorCode;
import com.clientservice.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @param response HTTP响应
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(final BusinessException e, 
                                                  final jakarta.servlet.http.HttpServletResponse response) {
        log.warn("业务异常: {}", e.getMessage());
        
        // 根据错误码设置对应的HTTP状态码
        String code = e.getCode();
        if (ErrorCode.UNAUTHORIZED.equals(code)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        } else if (ErrorCode.FORBIDDEN.equals(code)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        } else if (ErrorCode.NOT_FOUND.equals(code)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } else if (ErrorCode.BAD_REQUEST.equals(code)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } else if (ErrorCode.TOO_MANY_REQUESTS.equals(code)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        } else {
            // 默认返回200，但错误码在响应体中
            response.setStatus(HttpStatus.OK.value());
        }
        
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleValidationException(final Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                message = fieldError.getDefaultMessage();
            }
        }
        log.warn("参数校验异常: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理缺少请求参数异常
     *
     * @param e 缺少请求参数异常
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMissingServletRequestParameterException(final MissingServletRequestParameterException e) {
        String message = String.format("缺少必需参数: %s", e.getParameterName());
        log.warn("缺少请求参数异常: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理参数校验异常（@Validated 在方法参数上时抛出）
     */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleConstraintViolationException(final jakarta.validation.ConstraintViolationException e) {
        StringBuilder message = new StringBuilder("参数校验失败: ");
        e.getConstraintViolations().forEach(v -> 
            message.append(v.getPropertyPath()).append(" ").append(v.getMessage()).append("; "));
        log.warn("参数校验异常: {}", message);
        return Result.badRequest(message.toString().trim());
    }

    /**
     * 处理请求体解析异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("请求体解析异常: {}", e.getMessage());
        return Result.badRequest("请求体格式错误，请检查JSON格式是否正确");
    }

    /**
     * 处理参数类型不匹配异常（如字符串转数字失败）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException e) {
        String message = String.format("参数 '%s' 类型不正确", e.getName());
        log.warn("参数类型不匹配: {}", message);
        return Result.badRequest(message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleIllegalArgumentException(final IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.badRequest(e.getMessage());
    }

    /**
     * 处理静态资源未找到异常
     *
     * @param e 资源未找到异常
     * @return 错误响应
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Object> handleNoResourceFoundException(final NoResourceFoundException e) {
        String resourcePath = e.getResourcePath();
        // 静态资源未找到，返回404，但不记录为错误（这是正常的，比如sw.js等前端资源）
        log.debug("静态资源未找到: {}", resourcePath);
        
        // 如果是访问前端页面路径，给出友好提示
        if (resourcePath != null && (resourcePath.equals("portal") || resourcePath.startsWith("portal/"))) {
            // 开发环境：前端通过Vite运行在3000端口
            // 生产环境：前端静态资源应部署在 src/main/resources/static/portal/ 目录
            return Result.notFound("前端页面未找到。开发环境请访问 http://localhost:3000，生产环境请确保前端静态资源已部署。");
        }
        
        return Result.notFound("资源未找到: " + resourcePath);
    }

    /**
     * 处理数据库相关异常
     *
     * @param e 数据库异常
     * @return 错误响应
     */
    @ExceptionHandler({
        org.springframework.dao.DataAccessException.class,
        java.sql.SQLException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleDatabaseException(final Exception e) {
        log.error("数据库异常", e);
        // 不泄露详细的数据库错误信息，返回通用错误信息
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR, "数据操作失败，请稍后重试");
    }

    /**
     * 处理网络相关异常
     *
     * @param e 网络异常
     * @return 错误响应
     */
    @ExceptionHandler({
        java.net.ConnectException.class,
        java.net.SocketTimeoutException.class,
        java.io.IOException.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleNetworkException(final Exception e) {
        log.error("网络异常", e);
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR, "网络连接失败，请稍后重试");
    }

    /**
     * 处理其他异常
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleException(final Exception e) {
        log.error("系统异常", e);
        // 不泄露系统内部错误信息，返回通用错误信息
        return Result.error(ErrorCode.INTERNAL_SERVER_ERROR, "系统异常，请联系管理员");
    }
}
