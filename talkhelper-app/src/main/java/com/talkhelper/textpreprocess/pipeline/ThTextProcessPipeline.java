package com.talkhelper.textpreprocess.pipeline;

import com.talkhelper.common.constant.ThLogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ThTextProcessPipeline {

    private final List<ThTextProcessHandler> handlers;

    public ThTextProcessPipeline(List<ThTextProcessHandler> handlers) {
        this.handlers = handlers;
        log.info("文本处理管道初始化完成, 注册处理器数量: {}", handlers.size());
    }

    public ThTextProcessContext execute(ThTextProcessContext context) {
        log.info(ThLogConstants.LOG_SEPARATOR_START, "文本处理管道");

        long startTime = System.currentTimeMillis();

        try {
            for (ThTextProcessHandler handler : handlers) {
                if (!handler.shouldHandle(context)) {
                    log.debug(ThLogConstants.HANDLER_SKIP, handler.getName());
                    continue;
                }

                log.info(ThLogConstants.HANDLER_START, handler.getName());
                long handlerStartTime = System.currentTimeMillis();

                try {
                    executeWithRetry(handler, context);

                    long handlerEndTime = System.currentTimeMillis();
                    log.info(ThLogConstants.HANDLER_SUCCESS,
                            handler.getName(), handlerEndTime - handlerStartTime);

                } catch (Exception e) {
                    log.error(ThLogConstants.HANDLER_ERROR, handler.getName(), e);
                    throw new RuntimeException(
                            String.format("[%s] 处理失败: %s", handler.getName(), e.getMessage()), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info(ThLogConstants.LOG_SEPARATOR_END, "文本处理管道", endTime - startTime);

            return context;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error(ThLogConstants.LOG_SEPARATOR_ERROR, "文本处理管道", endTime - startTime, e);
            throw e;
        }
    }

    private void executeWithRetry(ThTextProcessHandler handler, ThTextProcessContext context) throws Exception {
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
                            handler.getName(), attempt + 1, actualDelay, attempt + 1, maxRetry, e.getMessage());
                    Thread.sleep(actualDelay);
                }
            }
        }

        throw lastException;
    }

    public List<ThTextProcessHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
}
