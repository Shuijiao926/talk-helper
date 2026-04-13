package com.talkhelper.task.pipeline;

/**
 * 任务创建处理器接口
 */
public interface ThTaskCreateHandler {

    /**
     * 处理任务创建
     *
     * @param context 上下文
     */
    void handle(ThTaskCreateContext context);

    /**
     * 获取处理器顺序(越小越先执行)
     */
    default int getOrder() {
        return 0;
    }
}
