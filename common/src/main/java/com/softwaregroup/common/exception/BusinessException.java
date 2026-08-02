package com.softwaregroup.common.exception;

/**
 * 业务异常
 *
 * 用于微服务中的业务逻辑错误：
 * <pre>
 * throw new BusinessException("用户名已存在");
 * throw new BusinessException(400, "参数错误");
 * </pre>
 */
public class BusinessException extends RuntimeException {

    private int code = 500;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
