package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ThAudioFinalizeHandler implements ThAudioProcessHandler {

    private final ThObjectStorageGateway storageGateway;

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
        log.info("[{}] Start audio finalize, segments={}", getName(), audioPaths.size());

        String concatPath = ThFfmpegUtils.concatAudio(audioPaths);
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 85, "Audio concat completed");
        }

        String mp3Path = ThFfmpegUtils.normalizeVolume(concatPath);
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 90, "Audio conversion completed");
        }

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

        String taskId = context.getTaskId();
        if (taskId == null || taskId.isEmpty()) {
            taskId = "default-" + System.currentTimeMillis();
        }
        String objectKey = "podcasts/" + taskId + ".mp3";
        byte[] mp3Bytes = Files.readAllBytes(mp3File.toPath());
        String audioUrl = storageGateway.uploadBytes(objectKey, mp3Bytes, "audio/mpeg");
        context.setAudioUrl(audioUrl);

        ThFfmpegUtils.deleteTempFile(concatPath);
        for (String path : audioPaths) {
            ThFfmpegUtils.deleteTempFile(path);
        }

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 100, "Audio generation completed");
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
