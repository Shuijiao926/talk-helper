package com.talkhelper.textpreprocess.pipeline;

import com.talkhelper.common.constant.ThLogConstants;
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
        log.info(ThLogConstants.LOG_SEPARATOR_START, "文本处理管道");
        
        long startTime = System.currentTimeMillis();
        
        try {
            for (ThTextProcessHandler handler : handlers) {
                // 检查是否应该执行此处理器
                if (!handler.shouldHandle(context)) {
                    log.debug(ThLogConstants.HANDLER_SKIP, handler.getName());
                    continue;
                }

                log.info(ThLogConstants.HANDLER_START, handler.getName());
                long handlerStartTime = System.currentTimeMillis();

                try {
                    // 执行处理器
                    handler.handle(context);
                    
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

    /**
     * 获取所有已注册的处理器
     */
    public List<ThTextProcessHandler> getHandlers() {
        return new ArrayList<>(handlers);
    }
}
