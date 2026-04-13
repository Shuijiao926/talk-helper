package com.talkhelper.task.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 任务创建Pipeline
 * 按顺序执行各个Handler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThTaskCreatePipeline {

    private final List<ThTaskCreateHandler> handlers;

    /**
     * 执行任务创建流程
     */
    public void execute(ThTaskCreateContext context) {
        log.debug("开始执行任务创建Pipeline, Handler数量: {}", handlers.size());

        // 按order排序
        handlers.sort(Comparator.comparingInt(ThTaskCreateHandler::getOrder));

        for (ThTaskCreateHandler handler : handlers) {
            try {
                log.debug("执行Handler: {}", handler.getClass().getSimpleName());
                handler.handle(context);

                // 如果处理失败,中断流程
                if (!context.isSuccess() && context.getErrorMessage() != null) {
                    log.error("任务创建失败: {}", context.getErrorMessage());
                    return;
                }
            } catch (Exception e) {
                log.error("Handler执行异常: {}", handler.getClass().getSimpleName(), e);
                context.setSuccess(false);
                context.setErrorMessage("Handler执行异常: " + e.getMessage());
                return;
            }
        }

        log.info("任务创建Pipeline执行完成, taskId={}", context.getTaskId());
    }
}
