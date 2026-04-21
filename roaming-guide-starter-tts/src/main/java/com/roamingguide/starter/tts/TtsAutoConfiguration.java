package com.roamingguide.starter.tts;

import com.roamingguide.starter.tts.dashscope.DashScopeTtsClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * TTS 语音合成自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(TtsProperties.class)
public class TtsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TtsClient.class)
    @ConditionalOnProperty(name = "spring.ai.dashscope.api-key")
    public DashScopeTtsClient dashScopeTtsClient(TtsProperties ttsProperties) {
        return new DashScopeTtsClient(ttsProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TtsService ttsService(TtsClient ttsClient, TtsProperties ttsProperties) {
        return new TtsService(ttsClient, ttsProperties);
    }
}
