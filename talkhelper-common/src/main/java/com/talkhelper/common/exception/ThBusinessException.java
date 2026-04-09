package com.talkhelper.common.exception;

/**
 * 业务异常类
 */
public class ThBusinessException extends RuntimeException {

    public ThBusinessException(String message) {
        super(message);
    }

    public ThBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
