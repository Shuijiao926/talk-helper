package com.talkhelper.common.result;

import lombok.Data;

/**
 * 统一返回结果类
 */
@Data
public class ThResult<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> ThResult<T> success(T data) {
        ThResult<T> result = new ThResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    public static <T> ThResult<T> error(String message) {
        ThResult<T> result = new ThResult<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }
}
