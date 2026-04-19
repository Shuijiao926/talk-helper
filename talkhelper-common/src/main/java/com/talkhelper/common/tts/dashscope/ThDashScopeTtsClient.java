package com.talkhelper.common.tts.dashscope;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisAudioFormat;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.talkhelper.common.tts.ThTtsClient;
import com.talkhelper.common.tts.ThTtsConfig;
import com.talkhelper.common.tts.ThTtsRequest;
import com.talkhelper.common.tts.ThTtsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * DashScope CosyVoice TTS客户端实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.ai.dashscope.api-key")
public class ThDashScopeTtsClient implements ThTtsClient {

    private final ThTtsConfig ttsConfig;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Override
    public String getType() {
        return "dashscope";
    }

    @Override
    public ThTtsResponse synthesize(ThTtsRequest request) {
        try {
            String voice = request.getVoice() != null ? request.getVoice() : ttsConfig.getDefaultVoice();
            String model = request.getModel() != null ? request.getModel() : ttsConfig.getModel();

            log.debug("DashScope TTS合成, model={}, voice={}, 文本长度={}", model, voice,
                    request.getText() != null ? request.getText().length() : 0);

            SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                    .apiKey(apiKey)
                    .model(model)
                    .voice(voice)
                    .format(SpeechSynthesisAudioFormat.WAV_22050HZ_MONO_16BIT)
                    .build();

            SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);
            ByteBuffer audioBuffer = synthesizer.call(request.getText());

            if (audioBuffer == null || audioBuffer.remaining() == 0) {
                log.warn("DashScope TTS返回音频数据为空");
                return ThTtsResponse.failure("TTS返回音频数据为空");
            }

            byte[] audioBytes = new byte[audioBuffer.remaining()];
            audioBuffer.get(audioBytes);

            // 检测是否为WAV格式（RIFF header），CosyVoice默认返回WAV
            String format = "wav";
            if (audioBytes.length < 4 || audioBytes[0] != 'R' || audioBytes[1] != 'I'
                    || audioBytes[2] != 'F' || audioBytes[3] != 'F') {
                // 没有RIFF header，可能是裸PCM数据，添加WAV header
                log.debug("TTS返回裸PCM数据，添加WAV header");
                audioBytes = addWavHeader(audioBytes, 24000, 16, 1);
            }

            log.info("DashScope TTS合成成功, 音频大小={}bytes", audioBytes.length);
            return ThTtsResponse.success(audioBytes, format);

        } catch (Exception e) {
            log.error("DashScope TTS合成失败", e);
            return ThTtsResponse.failure("DashScope TTS合成失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * 为裸PCM数据添加WAV文件头
     *
     * @param pcmData    PCM原始数据
     * @param sampleRate 采样率
     * @param bitDepth   位深度
     * @param channels   声道数
     */
    private byte[] addWavHeader(byte[] pcmData, int sampleRate, int bitDepth, int channels) {
        int dataLength = pcmData.length;
        int totalLength = dataLength + 36;
        int byteRate = sampleRate * channels * bitDepth / 8;
        int blockAlign = channels * bitDepth / 8;

        byte[] header = new byte[44];
        // RIFF header
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        writeInt(header, 4, totalLength);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        // fmt sub-chunk
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        writeInt(header, 16, 16); // sub-chunk size
        writeShort(header, 20, (short) 1); // PCM format
        writeShort(header, 22, (short) channels);
        writeInt(header, 24, sampleRate);
        writeInt(header, 28, byteRate);
        writeShort(header, 32, (short) blockAlign);
        writeShort(header, 34, (short) bitDepth);
        // data sub-chunk
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        writeInt(header, 40, dataLength);

        byte[] wavData = new byte[44 + dataLength];
        System.arraycopy(header, 0, wavData, 0, 44);
        System.arraycopy(pcmData, 0, wavData, 44, dataLength);
        return wavData;
    }

    private void writeInt(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
        buffer[offset + 2] = (byte) ((value >> 16) & 0xFF);
        buffer[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private void writeShort(byte[] buffer, int offset, short value) {
        buffer[offset] = (byte) (value & 0xFF);
        buffer[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }
}
