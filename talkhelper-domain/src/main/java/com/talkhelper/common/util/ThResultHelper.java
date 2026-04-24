package com.talkhelper.common.util;

import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.common.result.ThResult;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 统一响应工具类
 * 封装Controller层的通用响应逻辑，提高代码内聚性
 * 注意：不再捕获异常，异常交由全局异常处理器处理
 */
@Slf4j
public class ThResultHelper {

    /**
     * 执行业务逻辑并返回统一结果
     * 不捕获异常，让异常自然抛出到全局异常处理器
     *
     * @param businessLogic 业务逻辑Supplier
     * @param <T>           返回数据类型
     * @return 统一响应结果
     */
    public static <T> ThResult<T> execute(Supplier<T> businessLogic) {
        T result = businessLogic.get();
        return wrapResult(result);
    }

    /**
     * 包装业务结果为统一响应
     * 支持带有status字段的VO对象自动判断成功与否
     *
     * @param result 业务结果
     * @param <T>    返回数据类型
     * @return 统一响应结果
     */
    @SuppressWarnings("unchecked")
    private static <T> ThResult<T> wrapResult(T result) {
        // 如果结果是ThResult类型，直接返回
        if (result instanceof ThResult) {
            return (ThResult<T>) result;
        }
        
        // 其他类型直接包装为成功结果
        return ThResult.success(result);
    }
}
