package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

/**
 * 步骤6：多轨混音处理器（BGM + 音效 + 人声）
 * 播客的灵魂步骤，决定产品质感
 */
@Slf4j
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
        
        String outputPath;
        if (bgmPath != null && new File(bgmPath).exists()) {
            // 2. 执行混音命令（人声100%，BGM 25%，淡入淡出3秒）
            outputPath = ThFfmpegUtils.mixAudio(vocalPath, bgmPath, 0.25, 3.0);
            log.info("[{}] 混音完成（含BGM）: {}", getName(), outputPath);
        } else {
            // 无BGM时直接使用人声音轨
            log.info("[{}] 未找到BGM文件，跳过混音，直接使用人声音轨", getName());
            outputPath = vocalPath;
        }
        
        context.setMixedAudioPath(outputPath);
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 90, "混音完成");
        }
    }

    /**
     * 根据段落选择合适的BGM
     * 优先使用脚本中标注的BGM标签，否则使用默认BGM
     */
    private String selectBgm(List<ThAudioProcessContext.PodcastSegment> segments) {
        if (segments != null) {
            for (ThAudioProcessContext.PodcastSegment segment : segments) {
                if (segment.getBgmTag() != null && !segment.getBgmTag().isEmpty()) {
                    String bgmFile = "/assets/bgm/" + segment.getBgmTag() + ".mp3";
                    if (new File(bgmFile).exists()) {
                        log.info("使用脚本标注的BGM: {}", segment.getBgmTag());
                        return bgmFile;
                    }
                }
            }
        }
        // 默认BGM路径
        return "/assets/bgm/default.mp3";
    }
}
