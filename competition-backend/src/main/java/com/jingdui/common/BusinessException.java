package com.jingdui.common;

/**
 * 业务异常，用于在 Service 层抛出后由全局异常处理器统一捕获
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
