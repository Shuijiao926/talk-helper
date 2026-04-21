package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.entity.ThAudioSegmentEntity;
import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.audio.service.ThAudioSegmentService;
import com.roamingguide.starter.storage.StorageProperties;
import com.roamingguide.starter.storage.ObjectStorageFactory;
import com.roamingguide.starter.tts.TtsProperties;
import com.roamingguide.starter.tts.TtsService;
import com.talkhelper.common.util.ThFfmpegUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 步骤2：并行 TTS 语音合成处理器
 * 对标 TwoCast 的 genParts() + p-limit 并发控制
 * 每条目内置重试（3次指数退避），避免 handler 级别重试导致全量重跑
 * 每个片段合成后立即上传 OSS，并批量写入 th_audio_segment 表
 */
@Slf4j
@RequiredArgsConstructor
public class ThTtsSynthesisHandler implements ThAudioProcessHandler {

    private static final int ITEM_MAX_RETRY = 3;
    private static final long ITEM_RETRY_BASE_DELAY_MS = 2000;

    private final TtsService ttsService;
    private final TtsProperties ttsProperties;
    private final ObjectStorageFactory storageFactory;
    private final StorageProperties storageProperties;
    private final ThAudioSegmentService audioSegmentService;

    @Override
    public String getName() {
        return "并行TTS语音合成处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getScriptItems() != null && context.getTtsAudioPaths() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        List<ThAudioProcessContext.ScriptItem> items = context.getScriptItems();
        int concurrency = ttsProperties.getConcurrency();
        int total = items.size();

        log.info("[{}] 开始并行TTS合成, 共 {} 个条目, 并发度={}", getName(), total, concurrency);

        Semaphore semaphore = new Semaphore(concurrency);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        String[] audioPaths = new String[total];
        ThAudioProcessContext.SegmentInfo[] segmentInfos = new ThAudioProcessContext.SegmentInfo[total];
        AtomicInteger completed = new AtomicInteger(0);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < total; i++) {
                final int idx = i;
                ThAudioProcessContext.ScriptItem item = items.get(i);

                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        semaphore.acquire();
                        try {
                            log.info("[{}] TTS合成 {}/{}, role={}, 文本长度={}",
                                    getName(), idx + 1, total, item.getRole(),
                                    item.getText().length());

                            // 1. 单条目级别重试（指数退避），获取本地路径和音频字节
                            TtsService.TtsSynthesisResult result = synthesizeWithRetry(
                                    item.getText(), item.getRole(), idx);
                            audioPaths[idx] = result.getLocalPath();

                            // 2. 上传 OSS（尽力而为，不阻塞主流程）
                            String ossUrl = uploadToOss(result, context.getTaskId(), idx);

                            // 3. 获取音频时长（尽力而为）
                            double duration = getAudioDuration(result.getLocalPath());

                            // 4. 构建 SegmentInfo
                            segmentInfos[idx] = ThAudioProcessContext.SegmentInfo.builder()
                                    .segmentIndex(idx)
                                    .role(item.getRole())
                                    .localPath(result.getLocalPath())
                                    .ossUrl(ossUrl)
                                    .fileSize(result.getAudioData().length)
                                    .format(result.getFormat())
                                    .duration(duration)
                                    .build();

                            int done = completed.incrementAndGet();
                            if (context.getProgressCallback() != null && context.getTaskId() != null) {
                                int progress = 10 + (int) ((double) done / total * 70);
                                context.getProgressCallback().updateProgress(
                                        context.getTaskId(),
                                        progress,
                                        String.format("TTS合成中 (%d/%d)", done, total));
                            }
                        } finally {
                            semaphore.release();
                        }
                    } catch (Exception e) {
                        throw new CompletionException(
                                new RuntimeException(String.format(
                                        "TTS合成失败 [%d] role=%s: %s",
                                        idx, item.getRole(), e.getMessage()), e));
                    }
                }, executor));
            }

            // 等待全部完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
        }

        context.setTtsAudioPaths(Arrays.asList(audioPaths));
        context.setSegmentInfos(Arrays.asList(segmentInfos));

        // 批量写入 DB（尽力而为，不阻塞管道后续步骤）
        persistSegments(segmentInfos, context.getTaskId());

        log.info("[{}] TTS合成完成, 共 {} 个音频片段", getName(), total);

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 80, "TTS合成完成");
        }
    }

    /**
     * 上传音频片段到 OSS
     * 失败不抛异常，返回 null
     */
    private String uploadToOss(TtsService.TtsSynthesisResult result, String taskId, int idx) {
        try {
            String objectKey = "audio-segments/" + taskId + "/" + idx + "." + result.getFormat();
            String contentType = "wav".equals(result.getFormat()) ? "audio/wav" : "audio/" + result.getFormat();
            String ossUrl = storageFactory.getActiveStorage().uploadBytes(
                    result.getAudioData(),
                    storageProperties.getDefaultBucket(),
                    objectKey,
                    contentType);
            log.debug("[{}] 片段[{}] 已上传OSS: {}", getName(), idx, ossUrl);
            return ossUrl;
        } catch (Exception e) {
            log.warn("[{}] 片段[{}] OSS上传失败，不影响主流程: {}", getName(), idx, e.getMessage());
            return null;
        }
    }

    /**
     * 获取音频时长，失败返回 0
     */
    private double getAudioDuration(String localPath) {
        try {
            return ThFfmpegUtils.getAudioDuration(localPath);
        } catch (Exception e) {
            log.warn("[{}] 获取音频时长失败: {}", getName(), e.getMessage());
            return 0;
        }
    }

    /**
     * 批量持久化音频片段记录到 DB
     * 跳过无 OSS URL 的记录，失败不阻塞管道
     */
    private void persistSegments(ThAudioProcessContext.SegmentInfo[] segmentInfos, String taskId) {
        try {
            List<ThAudioSegmentEntity> entities = new ArrayList<>();
            for (ThAudioProcessContext.SegmentInfo info : segmentInfos) {
                if (info == null || info.getOssUrl() == null) {
                    continue;
                }
                entities.add(ThAudioSegmentEntity.builder()
                        .segmentId(UUID.randomUUID().toString())
                        .taskId(taskId)
                        .segmentIndex(info.getSegmentIndex())
                        .role(info.getRole())
                        .audioUrl(info.getOssUrl())
                        .fileSize(info.getFileSize())
                        .format(info.getFormat())
                        .duration(info.getDuration() > 0 ? info.getDuration() : null)
                        .build());
            }
            if (!entities.isEmpty()) {
                audioSegmentService.batchSaveSegments(entities);
            }
        } catch (Exception e) {
            log.error("[{}] 音频片段记录写入DB失败，不影响主流程: {}", getName(), e.getMessage(), e);
        }
    }

    /**
     * 带重试的单条 TTS 合成
     * 指数退避：2s → 4s → 8s
     */
    private TtsService.TtsSynthesisResult synthesizeWithRetry(String text, String role, int idx) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt <= ITEM_MAX_RETRY; attempt++) {
            try {
                return ttsService.synthesizeToFileAndBytes(text, role);
            } catch (Exception e) {
                lastException = e;
                if (attempt < ITEM_MAX_RETRY) {
                    long delay = ITEM_RETRY_BASE_DELAY_MS * (1L << attempt);
                    log.warn("[{}] TTS条目[{}] 第{}次失败, {}ms后重试: {}",
                            getName(), idx, attempt + 1, delay, e.getMessage());
                    Thread.sleep(delay);
                }
            }
        }
        throw lastException;
    }
}
