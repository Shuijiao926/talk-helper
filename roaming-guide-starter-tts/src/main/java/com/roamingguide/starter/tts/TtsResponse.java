package com.roamingguide.starter.tts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TTS语音合成响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TtsResponse {

    /**
     * 合成的音频原始字节数据
     */
    private byte[] audioData;

    /**
     * 音频格式（wav/pcm）
     */
    private String format;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 失败原因
     */
    private String errorMessage;

    public static TtsResponse success(byte[] audioData, String format) {
        TtsResponse response = new TtsResponse();
        response.setAudioData(audioData);
        response.setFormat(format);
        response.setSuccess(true);
        return response;
    }

    public static TtsResponse failure(String errorMessage) {
        TtsResponse response = new TtsResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
