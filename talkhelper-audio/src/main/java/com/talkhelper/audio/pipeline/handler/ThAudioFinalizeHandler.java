package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.roamingguide.starter.storage.StorageProperties;
import com.roamingguide.starter.storage.ObjectStorageFactory;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;

/**
 * 步骤3：音频拼接 + 上传处理器
 * 合并原来的 VocalConcat + AudioMixing + AudioFinalize 三步为一步
 * 对标 TwoCast 的 Buffer.concat + S3 upload
 */
@Slf4j
@RequiredArgsConstructor
public class ThAudioFinalizeHandler implements ThAudioProcessHandler {

    private final ObjectStorageFactory storageFactory;
    private final StorageProperties storageProperties;

    @Override
    public String getName() {
        return "音频拼接上传处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getTtsAudioPaths() != null && context.getAudioUrl() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        List<String> audioPaths = context.getTtsAudioPaths();
        log.info("[{}] 开始音频拼接, 共 {} 个片段", getName(), audioPaths.size());

        // 1. FFmpeg 拼接所有音频片段
        String concatPath = ThFfmpegUtils.concatAudio(audioPaths);
        log.info("[{}] 音频拼接完成: {}", getName(), concatPath);

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 85, "音频拼接完成");
        }

        // 2. 音量标准化 + 转 MP3
        String mp3Path = ThFfmpegUtils.normalizeVolume(concatPath);
        log.info("[{}] MP3转换完成: {}", getName(), mp3Path);

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 90, "格式转换完成");
        }

        // 3. 计算元数据
        File mp3File = new File(mp3Path);
        double duration = ThFfmpegUtils.getAudioDuration(mp3Path);
        String fileHash = calculateFileHash(mp3Path);

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
        String taskId = context.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            taskId = "default-" + System.currentTimeMillis();
        }
        String objectKey = "podcasts/" + taskId + ".mp3";
        byte[] mp3Bytes = Files.readAllBytes(mp3File.toPath());
        String audioUrl = storageFactory.getActiveStorage().uploadBytes(
                mp3Bytes, storageProperties.getDefaultBucket(), objectKey, "audio/mpeg");
        context.setAudioUrl(audioUrl);

        log.info("[{}] 音频已上传: {}, 时长={}s, 大小={}bytes",
                getName(), audioUrl, duration, mp3File.length());

        // 5. 清理临时文件
        ThFfmpegUtils.deleteTempFile(concatPath);
        for (String path : audioPaths) {
            ThFfmpegUtils.deleteTempFile(path);
        }

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 100, "音频生成完成");
        }
    }

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
