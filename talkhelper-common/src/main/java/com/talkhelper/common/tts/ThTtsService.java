package com.talkhelper.common.tts;

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
     * 合成语音并写入临时文件
     *
     * @param text 待合成文本
     * @param role 角色名称（主持人/嘉宾/旁白）
     * @return 临时WAV文件的绝对路径
     */
    public String synthesizeToFile(String text, String role) throws IOException {
        // 角色→音色映射
        String voice = ttsConfig.getVoiceMapping().getOrDefault(role, ttsConfig.getDefaultVoice());
        log.debug("TTS合成: role={}, voice={}, 文本长度={}", role, voice, text.length());

        // 调用TTS客户端
        ThTtsRequest request = ThTtsRequest.builder()
                .text(text)
                .voice(voice)
                .build();

        ThTtsResponse response = ttsClient.synthesize(request);

        if (!response.isSuccess()) {
            throw new RuntimeException("TTS合成失败: " + response.getErrorMessage());
        }

        // 写入临时文件
        String fileName = "tts_" + UUID.randomUUID().toString().substring(0, 8) + ".wav";
        Path tempFile = Files.createTempFile("talkhelper_", "_" + fileName);
        Files.write(tempFile, response.getAudioData());

        log.debug("TTS音频写入: {}, 大小={}bytes", tempFile, response.getAudioData().length);
        return tempFile.toAbsolutePath().toString();
    }

    /**
     * 检查TTS服务是否可用
     */
    public boolean isAvailable() {
        return ttsClient.isAvailable();
    }
}
