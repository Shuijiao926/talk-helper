package com.talkhelper.audio.pipeline.config;

import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.audio.pipeline.handler.*;
import com.talkhelper.audio.service.ThAudioSegmentService;
import com.roamingguide.starter.storage.StorageProperties;
import com.roamingguide.starter.storage.ObjectStorageFactory;
import com.roamingguide.starter.tts.TtsProperties;
import com.roamingguide.starter.tts.TtsService;
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

    private final TtsService ttsService;
    private final TtsProperties ttsProperties;
    private final ObjectStorageFactory storageFactory;
    private final StorageProperties storageProperties;
    private final ThAudioSegmentService audioSegmentService;

    /**
     * 步骤1：脚本 JSON 解析
     */
    @Bean
    @Order(1)
    public ThAudioProcessHandler scriptParseHandler() {
        return new ThScriptParseHandler(ttsProperties);
    }

    /**
     * 步骤2：并行 TTS 语音合成
     */
    @Bean
    @Order(2)
    public ThAudioProcessHandler ttsSynthesisHandler() {
        return new ThTtsSynthesisHandler(ttsService, ttsProperties, storageFactory, storageProperties, audioSegmentService);
    }

    /**
     * 步骤3：音频拼接 + 上传
     */
    @Bean
    @Order(3)
    public ThAudioProcessHandler audioFinalizeHandler() {
        return new ThAudioFinalizeHandler(storageFactory, storageProperties);
    }
}
