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
    private String model = "cosyvoice-v3-flash";

    /**
     * 默认音色
     */
    private String defaultVoice = "longxiaochun_v3";

    /**
     * TTS 并行度
     */
    private int concurrency = 3;

    /**
     * 单条 TTS 最长文本长度限制
     */
    private int maxStringLength = 20000;

    /**
     * 角色→音色映射
     */
    private Map<String, String> voiceMapping = new HashMap<>();
}
