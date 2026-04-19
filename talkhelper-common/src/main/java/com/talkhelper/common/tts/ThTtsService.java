package com.talkhelper.common.tts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * TTS语音合成服务层
 * 封装角色→音色映射和文件写入逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThTtsService {

    private final ThTtsClient ttsClient;
    private final ThTtsConfig ttsConfig;

    /**
     * TTS合成结果（同时包含本地路径和原始音频字节）
     */
    @Data
    @AllArgsConstructor
    public static class ThTtsSynthesisResult {
        private String localPath;
        private byte[] audioData;
        private String format;
    }

    /**
     * 合成语音并写入临时文件
     *
     * @param text 待合成文本
     * @param role 角色名称（主持人/嘉宾/旁白）
     * @return 临时WAV文件的绝对路径
     */
    public String synthesizeToFile(String text, String role) throws IOException {
        ThTtsSynthesisResult result = synthesizeToFileAndBytes(text, role);
        return result.getLocalPath();
    }

    /**
     * 合成语音，同时返回本地临时文件路径和原始音频字节
     * 一次合成避免重复I/O
     *
     * @param text 待合成文本
     * @param role 角色名称
     * @return 包含 localPath、audioData、format 的结果
     */
    public ThTtsSynthesisResult synthesizeToFileAndBytes(String text, String role) throws IOException {
        String voice = ttsConfig.getVoiceMapping().getOrDefault(role, ttsConfig.getDefaultVoice());
        log.debug("TTS合成: role={}, voice={}, 文本长度={}", role, voice, text.length());

        ThTtsRequest request = ThTtsRequest.builder()
                .text(text)
                .voice(voice)
                .build();

        ThTtsResponse response = ttsClient.synthesize(request);

        if (!response.isSuccess()) {
            throw new RuntimeException("TTS合成失败: " + response.getErrorMessage());
        }

        byte[] audioData = response.getAudioData();
        String format = response.getFormat() != null ? response.getFormat() : "wav";

        // 写入临时文件
        String fileName = "tts_" + UUID.randomUUID().toString().substring(0, 8) + "." + format;
        Path tempFile = Files.createTempFile("talkhelper_", "_" + fileName);
        Files.write(tempFile, audioData);

        log.debug("TTS音频写入: {}, 大小={}bytes", tempFile, audioData.length);
        return new ThTtsSynthesisResult(tempFile.toAbsolutePath().toString(), audioData, format);
    }

    /**
     * 检查TTS服务是否可用
     */
    public boolean isAvailable() {
        return ttsClient.isAvailable();
    }
}
