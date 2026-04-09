package com.talkhelper.web.annotation;

import java.lang.annotation.*;

/**
 * API日志注解
 * 用于标记需要记录日志的API接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ThApiLog {
    
    /**
     * API描述
     * @return API的功能描述
     */
    String value() default "";
    
    /**
     * 是否记录请求参数
     * @return true-记录, false-不记录
     */
    boolean logParams() default true;
    
    /**
     * 是否记录响应结果
     * @return true-记录, false-不记录
     */
    boolean logResult() default true;
}
