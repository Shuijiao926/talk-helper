package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 步骤5：纯人声音频拼接处理器
 * 将所有音频片段（含停顿）按顺序合并成完整人声音轨
 */
@Slf4j
@Component
public class ThVocalConcatHandler implements ThAudioProcessHandler {

    @Override
    public String getName() {
        return "人声音频拼接处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getPausedAudioPaths() != null && context.getFullVocalPath() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始拼接人声音频", getName());
        
        List<String> audioPaths = context.getPausedAudioPaths();
        
        // 使用FFmpeg concat demuxer合并音频
        String outputPath = ThFfmpegUtils.concatAudio(audioPaths);
        
        context.setFullVocalPath(outputPath);
        
        log.info("[{}] 人声拼接完成: {}", getName(), outputPath);
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 80, "人声拼接完成");
        }
    }
}
