package com.talkhelper.audio.pipeline.config;

import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.audio.pipeline.handler.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 音频处理管道配置
 * 定义7个处理器的执行顺序
 */
@Configuration
@RequiredArgsConstructor
public class ThAudioProcessPipelineConfig {

    /**
     * 步骤1：脚本结构化解析处理器
     */
    @Bean
    @Order(1)
    public ThAudioProcessHandler scriptParseHandler() {
        return new ThScriptParseHandler();
    }

    /**
     * 步骤2：文本分段切割处理器
     */
    @Bean
    @Order(2)
    public ThAudioProcessHandler textFragmentHandler() {
        return new ThTextFragmentHandler();
    }

    /**
     * 步骤3：TTS语音合成处理器
     */
    @Bean
    @Order(3)
    public ThAudioProcessHandler ttsSynthesisHandler() {
        return new ThTtsSynthesisHandler();
    }

    /**
     * 步骤4：静音停顿插入处理器
     */
    @Bean
    @Order(4)
    public ThAudioProcessHandler pauseInsertionHandler() {
        return new ThPauseInsertionHandler();
    }

    /**
     * 步骤5：人声音频拼接处理器
     */
    @Bean
    @Order(5)
    public ThAudioProcessHandler vocalConcatHandler() {
        return new ThVocalConcatHandler();
    }

    /**
     * 步骤6：多轨混音处理器
     */
    @Bean
    @Order(6)
    public ThAudioProcessHandler audioMixingHandler() {
        return new ThAudioMixingHandler();
    }

    /**
     * 步骤7：音频后期处理处理器
     */
    @Bean
    @Order(7)
    public ThAudioProcessHandler audioFinalizeHandler() {
        return new ThAudioFinalizeHandler();
    }
}
