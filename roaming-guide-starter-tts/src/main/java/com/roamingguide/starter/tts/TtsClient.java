package com.roamingguide.starter.tts;

/**
 * TTS客户端接口
 * 支持多厂商扩展（DashScope、Azure等）
 */
public interface TtsClient {

    /**
     * 获取提供商标识
     */
    String getType();

    /**
     * 执行语音合成
     */
    TtsResponse synthesize(TtsRequest request);

    /**
     * 检查服务可用性
     */
    boolean isAvailable();
}
