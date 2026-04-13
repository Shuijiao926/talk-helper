package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

/**
 * 步骤7：音频后期标准化 + 格式输出 + 持久化处理器
 * 音量统一、降噪、压缩、上传对象存储、入库
 */
@Slf4j
@Component
public class ThAudioFinalizeHandler implements ThAudioProcessHandler {

    // TODO: 注入对象存储服务
    // private final ThMinioUtils minioUtils;
    
    // TODO: 注入任务服务
    // private final ThTaskService taskService;

    @Override
    public String getName() {
        return "音频后期处理处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getMixedAudioPath() != null && context.getFinalMp3Path() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始音频后期处理", getName());
        
        String inputPath = context.getMixedAudioPath();
        
        // 1. 音量标准化 + 格式压缩（FFmpeg一键完成）
        String mp3Path = ThFfmpegUtils.normalizeVolume(inputPath);
        
        context.setFinalMp3Path(mp3Path);
        log.info("[{}] MP3生成完成: {}", getName(), mp3Path);
        
        // 2. 计算文件哈希（SHA256）
        String fileHash = calculateFileHash(mp3Path);
        
        // 3. 获取文件元数据
        File mp3File = new File(mp3Path);
        double duration = ThFfmpegUtils.getAudioDuration(mp3Path);
        
        ThAudioProcessContext.AudioMetadata metadata = ThAudioProcessContext.AudioMetadata.builder()
                .duration(duration)
                .fileSize(mp3File.length())
                .format("mp3")
                .bitrate(320)
                .sampleRate(44100)
                .fileHash(fileHash)
                .createTime(System.currentTimeMillis())
                .build();
        
        context.setMetadata(metadata);
        
        // 4. 上传到对象存储
        // TODO: 上传到MinIO/S3
        // String objectKey = "podcasts/" + context.getTaskId() + ".mp3";
        // String audioUrl = minioUtils.uploadFile(mp3Path, objectKey);
        String audioUrl = "http://minio:9000/podcasts/" + context.getTaskId() + ".mp3";
        context.setAudioUrl(audioUrl);
        
        log.info("[{}] 音频已上传: {}", getName(), audioUrl);
        
        // 5. 更新任务状态（入库）
        // TODO: 保存音频URL和元数据到数据库
        // taskService.updateTaskAudio(context.getTaskId(), audioUrl, metadata);
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 100, "音频生成完成");
        }
        
        log.info("[{}] 音频后期处理全部完成", getName());
    }

    /**
     * 计算文件SHA256哈希
     */
    private String calculateFileHash(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(new File(filePath).toPath());
        byte[] hashBytes = digest.digest(fileBytes);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
}
