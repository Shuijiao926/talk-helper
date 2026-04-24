package com.talkhelper.common.exception;

import com.talkhelper.common.result.ThErrorCode;
import lombok.Getter;

@Getter
public class ThBusinessException extends RuntimeException {

    private final ThErrorCode errorCode;

    public ThBusinessException(ThErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ThBusinessException(ThErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ThBusinessException(ThErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ThBusinessException(ThErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ThBusinessException(String message) {
        super(message);
        this.errorCode = ThErrorCode.INTERNAL_ERROR;
    }

    public ThBusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ThErrorCode.INTERNAL_ERROR;
    }
}
