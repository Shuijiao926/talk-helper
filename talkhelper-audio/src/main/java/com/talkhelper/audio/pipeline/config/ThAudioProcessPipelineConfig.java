package com.talkhelper.audio.pipeline.config;

import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.audio.pipeline.handler.*;
import com.talkhelper.common.config.ThStorageConfig;
import com.talkhelper.common.storage.ThObjectStorageFactory;
import com.talkhelper.common.tts.ThTtsConfig;
import com.talkhelper.common.tts.ThTtsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 音频处理管道配置（简化版 - 对标 TwoCast）
 * 3 个处理器：脚本解析 → 并行TTS → 拼接上传
 */
@Configuration
@RequiredArgsConstructor
public class ThAudioProcessPipelineConfig {

    private final ThTtsService ttsService;
    private final ThTtsConfig ttsConfig;
    private final ThObjectStorageFactory storageFactory;
    private final ThStorageConfig storageConfig;

    /**
     * 步骤1：脚本 JSON 解析
     */
    @Bean
    @Order(1)
    public ThAudioProcessHandler scriptParseHandler() {
        return new ThScriptParseHandler();
    }

    /**
     * 步骤2：并行 TTS 语音合成
     */
    @Bean
    @Order(2)
    public ThAudioProcessHandler ttsSynthesisHandler() {
        return new ThTtsSynthesisHandler(ttsService, ttsConfig);
    }

    /**
     * 步骤3：音频拼接 + 上传
     */
    @Bean
    @Order(3)
    public ThAudioProcessHandler audioFinalizeHandler() {
        return new ThAudioFinalizeHandler(storageFactory, storageConfig);
    }
}
