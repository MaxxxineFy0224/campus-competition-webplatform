package com.jingdui.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * 统一返回 Result 格式，前端根据 code 判断：
 *   401 → 未登录/Token 过期，前端弹出登录弹窗
 *   403 → 无权限
 *   400 → 参数校验失败
 *   500 → 服务器内部错误
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return Result.error(400, msg);
    }

    /**
     * 业务异常（包含 401/403/404 等）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusiness(BusinessException ex) {
        if (ex.getCode() == 401) {
            log.warn("未授权访问: {}", ex.getMessage());
        } else if (ex.getCode() == 403) {
            log.warn("无权限访问: {}", ex.getMessage());
        } else {
            log.warn("业务异常: {}", ex.getMessage());
        }
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error(500, "服务器内部错误: " + ex.getMessage());
    }
}