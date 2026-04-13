package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 步骤6：多轨混音处理器（BGM + 音效 + 人声）
 * 播客的灵魂步骤，决定产品质感
 */
@Slf4j
@Component
public class ThAudioMixingHandler implements ThAudioProcessHandler {

    @Override
    public String getName() {
        return "多轨混音处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getFullVocalPath() != null && context.getMixedAudioPath() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始多轨混音", getName());
        
        String vocalPath = context.getFullVocalPath();
        
        // 1. 选择BGM（根据脚本中的BGM标签）
        String bgmPath = selectBgm(context.getSegments());
        
        // 2. 执行混音命令（人声100%，BGM 25%，淡入淡出3秒）
        String outputPath = ThFfmpegUtils.mixAudio(vocalPath, bgmPath, 0.25, 3.0);
        
        context.setMixedAudioPath(outputPath);
        
        log.info("[{}] 混音完成: {}", getName(), outputPath);
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 90, "混音完成");
        }
    }

    /**
     * 根据段落选择合适的BGM
     */
    private String selectBgm(java.util.List<ThAudioProcessContext.PodcastSegment> segments) {
        // TODO: 实现BGM选择逻辑
        // 可以根据情绪、主题等选择不同BGM
        return "/assets/bgm/default.mp3";
    }
}
