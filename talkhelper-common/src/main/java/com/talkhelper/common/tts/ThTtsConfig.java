package com.talkhelper.common.tts;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TTS语音合成配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "talkhelper.tts")
public class ThTtsConfig {

    /**
     * TTS模型名称
     */
    private String model = "cosyvoice-v3.5-plus";

    /**
     * 默认音色
     */
    private String defaultVoice = "longxiaochun";

    /**
     * TTS 并行度（对标 TwoCast 的 p-limit concurrency）
     */
    private int concurrency = 3;

    /**
     * 角色→音色映射
     */
    private Map<String, String> voiceMapping = new HashMap<>();
}
