package com.talkhelper.audio.service;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 音频处理服务
 * 提供播客脚本到MP3音频的完整转换能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThAudioProcessService {

    private final ThAudioProcessPipeline pipeline;

    /**
     * 将播客脚本转换为MP3音频
     *
     * @param podcastScript 格式化的播客脚本（带标注）
     * @param taskId 任务ID（用于进度追踪）
     * @return 音频URL
     */
    public String generatePodcastAudio(String podcastScript, String taskId) {
        log.info("开始生成播客音频, taskId: {}", taskId);
        
        // 构建处理上下文
        ThAudioProcessContext context = ThAudioProcessContext.builder()
                .podcastScript(podcastScript)
                .taskId(taskId)
                .build();
        
        // 执行管道处理
        pipeline.execute(context);
        
        // 返回音频URL
        String audioUrl = context.getAudioUrl();
        log.info("播客音频生成完成, taskId: {}, URL: {}", taskId, audioUrl);
        
        return audioUrl;
    }

    /**
     * 获取支持的音频格式
     */
    public String[] getSupportedFormats() {
        return new String[]{"mp3", "wav", "ogg"};
    }
}
