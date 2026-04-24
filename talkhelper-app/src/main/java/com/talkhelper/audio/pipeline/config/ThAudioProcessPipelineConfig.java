package com.talkhelper.audio.pipeline.config;

import com.roamingguide.starter.tts.TtsProperties;
import com.roamingguide.starter.tts.TtsService;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.audio.pipeline.handler.ThAudioFinalizeHandler;
import com.talkhelper.audio.pipeline.handler.ThScriptParseHandler;
import com.talkhelper.audio.pipeline.handler.ThTtsSynthesisHandler;
import com.talkhelper.audio.service.ThAudioSegmentService;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
public class ThAudioProcessPipelineConfig {

    private final TtsService ttsService;
    private final TtsProperties ttsProperties;
    private final ThObjectStorageGateway storageGateway;
    private final ThAudioSegmentService audioSegmentService;

    @Bean
    @Order(1)
    public ThAudioProcessHandler scriptParseHandler() {
        return new ThScriptParseHandler(ttsProperties);
    }

    @Bean
    @Order(2)
    public ThAudioProcessHandler ttsSynthesisHandler() {
        return new ThTtsSynthesisHandler(ttsService, ttsProperties, storageGateway, audioSegmentService);
    }

    @Bean
    @Order(3)
    public ThAudioProcessHandler audioFinalizeHandler() {
        return new ThAudioFinalizeHandler(storageGateway);
    }
}
