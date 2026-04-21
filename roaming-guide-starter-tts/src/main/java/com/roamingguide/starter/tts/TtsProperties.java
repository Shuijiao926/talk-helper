package com.roamingguide.starter.tts;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * TTS语音合成配置属性
 */
@Data
@ConfigurationProperties(prefix = "roaming-guide.tts")
public class TtsProperties {

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
