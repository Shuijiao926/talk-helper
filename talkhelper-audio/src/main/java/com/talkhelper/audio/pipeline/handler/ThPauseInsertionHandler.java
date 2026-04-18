package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 步骤4：插入静音停顿处理器
 * 根据脚本中的停顿指令，在音频片段间插入对应时长的静音
 */
@Slf4j
public class ThPauseInsertionHandler implements ThAudioProcessHandler {

    @Override
    public String getName() {
        return "静音停顿插入处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getTtsAudioPaths() != null && context.getPausedAudioPaths() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始插入静音停顿", getName());
        
        List<String> ttsPaths = context.getTtsAudioPaths();
        List<ThAudioProcessContext.TextFragment> fragments = context.getFragments();
        List<String> pausedPaths = new ArrayList<>();
        
        for (int i = 0; i < ttsPaths.size(); i++) {
            // 添加音频片段
            pausedPaths.add(ttsPaths.get(i));
            
            // 如果有停顿时长，生成静音音频并添加
            if (i < fragments.size()) {
                Double pauseDuration = fragments.get(i).getPauseDuration();
                if (pauseDuration != null && pauseDuration > 0) {
                    log.info("[{}] 插入 {:.1f} 秒停顿", getName(), pauseDuration);
                    
                    // 使用FFmpeg生成静音音频
                    String silencePath = ThFfmpegUtils.generateSilence(pauseDuration);
                    pausedPaths.add(silencePath);
                }
            }
        }
        
        context.setPausedAudioPaths(pausedPaths);
        
        log.info("[{}] 停顿插入完成, 共 {} 个音频段", getName(), pausedPaths.size());
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 75, "停顿插入完成");
        }
    }
}
