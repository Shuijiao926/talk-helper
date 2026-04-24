package com.talkhelper.common.result;

import lombok.Data;

@Data
public class ThResult<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ThResult<T> success(T data) {
        ThResult<T> result = new ThResult<>();
        result.setCode(ThErrorCode.SUCCESS.getCode());
        result.setMessage(ThErrorCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> ThResult<T> success() {
        return success(null);
    }

    public static <T> ThResult<T> error(ThErrorCode errorCode) {
        ThResult<T> result = new ThResult<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        return result;
    }

    public static <T> ThResult<T> error(ThErrorCode errorCode, String message) {
        ThResult<T> result = new ThResult<>();
        result.setCode(errorCode.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> ThResult<T> error(String message) {
        ThResult<T> result = new ThResult<>();
        result.setCode(ThErrorCode.INTERNAL_ERROR.getCode());
        result.setMessage(message);
        return result;
    }
}
