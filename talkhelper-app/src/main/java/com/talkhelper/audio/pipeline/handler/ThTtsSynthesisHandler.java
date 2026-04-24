package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.entity.ThAudioSegmentEntity;
import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.audio.service.ThAudioSegmentService;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.common.util.ThFfmpegUtils;
import com.roamingguide.starter.tts.TtsProperties;
import com.roamingguide.starter.tts.TtsService;
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

@Slf4j
@RequiredArgsConstructor
public class ThTtsSynthesisHandler implements ThAudioProcessHandler {

    private static final int ITEM_MAX_RETRY = 3;
    private static final long ITEM_RETRY_BASE_DELAY_MS = 2000;

    private final TtsService ttsService;
    private final TtsProperties ttsProperties;
    private final ThObjectStorageGateway storageGateway;
    private final ThAudioSegmentService audioSegmentService;

    @Override
    public String getName() {
        return "并行 TTS 语音合成处理器";
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

        log.info("[{}] Start TTS synthesis, items={}, concurrency={}", getName(), total, concurrency);

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
                            TtsService.TtsSynthesisResult result = synthesizeWithRetry(item.getText(), item.getRole(), idx);
                            audioPaths[idx] = result.getLocalPath();

                            String ossUrl = uploadToStorage(result, context.getTaskId(), idx);
                            double duration = getAudioDuration(result.getLocalPath());

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
                                        String.format("TTS synthesis (%d/%d)", done, total)
                                );
                            }
                        } finally {
                            semaphore.release();
                        }
                    } catch (Exception e) {
                        throw new CompletionException(
                                new RuntimeException(String.format("TTS synthesis failed [%d] role=%s: %s",
                                        idx, item.getRole(), e.getMessage()), e)
                        );
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } finally {
            executor.shutdown();
        }

        context.setTtsAudioPaths(Arrays.asList(audioPaths));
        context.setSegmentInfos(Arrays.asList(segmentInfos));
        persistSegments(segmentInfos, context.getTaskId());

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 80, "TTS synthesis completed");
        }
    }

    private String uploadToStorage(TtsService.TtsSynthesisResult result, String taskId, int idx) {
        try {
            String objectKey = "audio-segments/" + taskId + "/" + idx + "." + result.getFormat();
            String contentType = "wav".equals(result.getFormat()) ? "audio/wav" : "audio/" + result.getFormat();
            return storageGateway.uploadBytes(objectKey, result.getAudioData(), contentType);
        } catch (Exception e) {
            log.warn("[{}] Failed to upload segment [{}]: {}", getName(), idx, e.getMessage());
            return null;
        }
    }

    private double getAudioDuration(String localPath) {
        try {
            return ThFfmpegUtils.getAudioDuration(localPath);
        } catch (Exception e) {
            log.warn("[{}] Failed to read audio duration: {}", getName(), e.getMessage());
            return 0;
        }
    }

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
            log.error("[{}] Failed to persist audio segments", getName(), e);
        }
    }

    private TtsService.TtsSynthesisResult synthesizeWithRetry(String text, String role, int idx) throws Exception {
        Exception lastException = null;
        for (int attempt = 0; attempt <= ITEM_MAX_RETRY; attempt++) {
            try {
                return ttsService.synthesizeToFileAndBytes(text, role);
            } catch (Exception e) {
                lastException = e;
                if (attempt < ITEM_MAX_RETRY) {
                    long delay = ITEM_RETRY_BASE_DELAY_MS * (1L << attempt);
                    log.warn("[{}] Retry TTS item [{}] after {}ms: {}", getName(), idx, delay, e.getMessage());
                    Thread.sleep(delay);
                }
            }
        }
        throw lastException;
    }
}
