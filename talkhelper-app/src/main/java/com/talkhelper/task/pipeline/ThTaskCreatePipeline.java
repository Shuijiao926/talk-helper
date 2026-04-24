package com.talkhelper.task.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThTaskCreatePipeline {

    private final List<ThTaskCreateHandler> handlers;

    public void execute(ThTaskCreateContext context) {
        handlers.sort(Comparator.comparingInt(ThTaskCreateHandler::getOrder));

        for (ThTaskCreateHandler handler : handlers) {
            try {
                log.debug("执行Handler: {}", handler.getClass().getSimpleName());
                executeWithRetry(handler, context);

                if (!context.isSuccess() && context.getErrorMessage() != null) {
                    log.error("任务创建失败: {}", context.getErrorMessage());
                    return;
                }
            } catch (Exception e) {
                log.error("Handler执行异常: {}", handler.getClass().getSimpleName(), e);
                context.setSuccess(false);
                context.setErrorMessage("Handler执行异常: " + e.getMessage());
                // 保留原始异常,方便排查
                throw new RuntimeException("Handler执行异常: " + handler.getClass().getSimpleName(), e);
            }
        }

        log.info("任务创建Pipeline执行完成, taskId={}", context.getTaskId());
    }

    private void executeWithRetry(ThTaskCreateHandler handler, ThTaskCreateContext context) {
        int maxRetry = handler.maxRetry();
        long delayMs = handler.retryDelayMs();
        Exception lastException = null;

        for (int attempt = 0; attempt <= maxRetry; attempt++) {
            try {
                handler.handle(context);
                return;
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxRetry) {
                    long actualDelay = delayMs * (1L << attempt);
                    log.warn("[{}] 第{}次执行失败, {}ms后重试({}/{}), 错误: {}",
                            handler.getClass().getSimpleName(), attempt + 1, actualDelay, attempt + 1, maxRetry, e.getMessage());
                    try {
                        Thread.sleep(actualDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Handler执行失败，已重试" + maxRetry + "次", lastException);
    }
}
