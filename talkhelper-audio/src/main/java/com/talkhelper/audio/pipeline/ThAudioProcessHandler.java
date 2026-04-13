package com.talkhelper.audio.pipeline;

/**
 * 音频处理管道处理器接口
 * 每个处理器负责管道中的一个步骤
 */
public interface ThAudioProcessHandler {

    /**
     * 获取处理器名称
     * @return 处理器名称
     */
    String getName();

    /**
     * 执行处理逻辑
     * @param context 处理上下文
     * @throws Exception 处理异常
     */
    void handle(ThAudioProcessContext context) throws Exception;

    /**
     * 判断是否应该执行此处理器
     * 可用于条件性执行某些步骤
     * @param context 处理上下文
     * @return true-执行, false-跳过
     */
    default boolean shouldHandle(ThAudioProcessContext context) {
        return true;
    }
}
