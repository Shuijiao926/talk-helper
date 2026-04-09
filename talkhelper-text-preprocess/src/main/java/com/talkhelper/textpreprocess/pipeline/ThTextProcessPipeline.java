package com.talkhelper.textpreprocess.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本处理管道执行器
 * 负责按顺序执行所有注册的处理器
 */
@Slf4j
@Component
public class ThTextProcessPipeline {

    private final List<ThTextProcessHandler> handlers;

    public ThTextProcessPipeline(List<ThTextProcessHandler> handlers) {
        this.handlers = handlers;
        log.info("文本处理管道初始化完成, 注册处理器数量: {}", handlers.size());
    }

    /**
     * 执行管道处理
     *
     * @param context 处理上下文
     * @return 处理后的上下文
     */
    public ThTextProcessContext execute(ThTextProcessContext context) {
        log.info("========== 开始执行文本处理管道 ==========");
        
        long startTime = System.currentTimeMillis();
        
        try {
            for (ThTextProcessHandler handler : handlers) {
                // 检查是否应该执行此处理器
                if (!handler.shouldHandle(context)) {
                    log.debug("[{}] 跳过执行", handler.getName());
                    continue;
                }

                log.info("[{}] 开始执行...", handler.getName());
                long handlerStartTime = System.currentTimeMillis();

                try {
                    // 执行处理器
                    handler.handle(context);
                    
                    long handlerEndTime = System.currentTimeMillis();
                    log.info("[{}] 执行成功, 耗时: {}ms", 
                            handler.getName(), handlerEndTime - handlerStartTime);
                    
                } catch (Exception e) {
                    log.error("[{}] 执行失败", handler.getName(), e);
                    throw new RuntimeException(
                            String.format("[%s] 处理失败: %s", handler.getName(), e.getMessage()), e);
                }
            }

            long endTime = System.currentTimeMillis();
            log.info("========== 文本处理管道执行完成, 总耗时: {}ms ==========", 
                    endTime - startTime);
            
            return context;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("========== 文本处理管道执行失败, 耗时: {}ms ==========", 
                    endTime - startTime, e);
            throw e;
        }
    }

    /**
     * 获取所有已注册的处理器
     */
    public List<ThTextProcessHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
}
