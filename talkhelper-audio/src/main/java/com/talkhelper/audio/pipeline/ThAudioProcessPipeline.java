package com.talkhelper.audio.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ThAudioProcessPipeline {

    private final List<ThAudioProcessHandler> handlers;

    public ThAudioProcessPipeline(List<ThAudioProcessHandler> handlers) {
        this.handlers = handlers;
        log.info("音频处理管道初始化完成, 注册处理器数量: {}", handlers.size());
    }

    public ThAudioProcessContext execute(ThAudioProcessContext context) {
        log.info("========== 音频处理管道开始 ==========");

        long startTime = System.currentTimeMillis();

        try {
            for (ThAudioProcessHandler handler : handlers) {
                if (!handler.shouldHandle(context)) {
                    log.debug("跳过处理器: {}", handler.getName());
                    continue;
                }

                log.info(">>> 执行处理器: [{}]", handler.getName());
                long handlerStartTime = System.currentTimeMillis();

                try {
                    executeWithRetry(handler, context);

                    long handlerEndTime = System.currentTimeMillis();
                    log.info("<<< 处理器完成: [{}], 耗时: {}ms",
                            handler.getName(), handlerEndTime - handlerStartTime);

                } catch (Exception e) {
                    log.error("处理器执行失败: [{}], 错误: {}", handler.getName(), e.getMessage(), e);
                    throw new RuntimeException(
                            String.format("[%s] 处理失败: %s", handler.getName(), e.getMessage()), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("========== 音频处理管道完成, 总耗时: {}ms ==========", endTime - startTime);

            return context;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("========== 音频处理管道失败, 耗时: {}ms ==========", endTime - startTime, e);
            throw e;
        }
    }

    private void executeWithRetry(ThAudioProcessHandler handler, ThAudioProcessContext context) throws Exception {
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

    public List<ThAudioProcessHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
}
