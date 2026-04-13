package com.talkhelper.common.aspect;

import com.talkhelper.common.annotation.ThApiLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * API日志切面
 * 统一处理API请求的日志记录
 */
@Slf4j
@Aspect
@Component
public class ThApiLogAspect {

    /**
     * 环绕通知，拦截带有 @ThApiLog 注解的方法
     */
    @Around("@annotation(com.talkhelper.common.annotation.ThApiLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名和注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ThApiLog apiLog = method.getAnnotation(ThApiLog.class);
        
        // 获取API描述
        String apiDescription = apiLog.value();
        String methodName = method.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        // 构建日志前缀
        String logPrefix = String.format("[%s.%s] %s", className, methodName, 
                apiDescription.isEmpty() ? methodName : apiDescription);
        
        // 记录请求开始日志
        log.info("{} - 请求开始", logPrefix);
        
        // 记录请求参数
        if (apiLog.logParams()) {
            Object[] args = joinPoint.getArgs();
            String params = formatParams(args);
            if (!params.isEmpty()) {
                log.info("{} - 请求参数: {}", logPrefix, params);
            }
        }
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录响应结果
            if (apiLog.logResult()) {
                log.info("{} - 响应结果: {}, 耗时: {}ms", logPrefix, result, executionTime);
            } else {
                log.info("{} - 执行成功, 耗时: {}ms", logPrefix, executionTime);
            }
            
            return result;
        } catch (Exception e) {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录异常日志
            log.error("{} - 执行异常: {}, 耗时: {}ms", logPrefix, e.getMessage(), executionTime, e);
            throw e;
        }
    }
    
    /**
     * 格式化请求参数
     * 过滤掉MultipartFile等大对象，避免日志过大
     */
    private String formatParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        
        return Arrays.stream(args)
                .map(arg -> {
                    // 过滤MultipartFile类型，只记录文件名
                    if (arg instanceof MultipartFile) {
                        MultipartFile file = (MultipartFile) arg;
                        return String.format("MultipartFile[name=%s, size=%d]", 
                                file.getOriginalFilename(), file.getSize());
                    }
                    // 其他类型正常toString
                    return arg != null ? arg.toString() : "null";
                })
                .collect(Collectors.joining(", "));
    }
}
