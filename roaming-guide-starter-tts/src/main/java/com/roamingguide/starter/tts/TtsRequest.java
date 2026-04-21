package com.roamingguide.starter.tts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TTS语音合成请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TtsRequest {

    /**
     * 待合成的文本内容
     */
    private String text;

    /**
     * 音色ID（如 "longxiaochun"）
     */
    private String voice;

    /**
     * 模型名称（可选，覆盖默认模型）
     */
    private String model;

    /**
     * 输出音频格式（默认 wav）
     */
    @Builder.Default
    private String format = "wav";
}
