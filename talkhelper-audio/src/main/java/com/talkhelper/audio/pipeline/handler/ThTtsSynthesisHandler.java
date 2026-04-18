package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.tts.ThTtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 步骤3：逐段TTS语音合成处理器
 * 调用DashScope CosyVoice为每个文本片段生成音频
 */
@Slf4j
@RequiredArgsConstructor
public class ThTtsSynthesisHandler implements ThAudioProcessHandler {

    private final ThTtsService ttsService;

    @Override
    public String getName() {
        return "TTS语音合成处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getFragments() != null && context.getTtsAudioPaths() == null;
    }

    @Override
    public int maxRetry() {
        return 3;
    }

    @Override
    public long retryDelayMs() {
        return 3000;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始TTS语音合成", getName());

        List<String> audioPaths = new ArrayList<>();
        int totalFragments = context.getFragments().size();

        for (int i = 0; i < totalFragments; i++) {
            ThAudioProcessContext.TextFragment fragment = context.getFragments().get(i);

            log.info("[{}] 合成第 {}/{} 个片段, role={}, 文本长度={}",
                    getName(), i + 1, totalFragments,
                    fragment.getRole(), fragment.getText().length());

            // 调用TTS服务合成音频并写入临时文件
            String audioPath = ttsService.synthesizeToFile(fragment.getText(), fragment.getRole());
            audioPaths.add(audioPath);

            // 上报进度
            if (context.getProgressCallback() != null && context.getTaskId() != null) {
                int progress = 28 + (int) ((i + 1.0) / totalFragments * 42); // 28%-70%
                context.getProgressCallback().updateProgress(
                        context.getTaskId(),
                        progress,
                        String.format("TTS合成中 (%d/%d)", i + 1, totalFragments)
                );
            }
        }

        context.setTtsAudioPaths(audioPaths);

        log.info("[{}] TTS合成完成, 共 {} 个音频片段", getName(), audioPaths.size());

        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 70, "TTS合成完成");
        }
    }
}
