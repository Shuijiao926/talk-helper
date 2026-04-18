package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.talkhelper.common.tts.ThTtsConfig;
import com.talkhelper.common.tts.ThTtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 步骤2：并行 TTS 语音合成处理器
 * 对标 TwoCast 的 genParts() + p-limit 并发控制
 */
@Slf4j
@RequiredArgsConstructor
public class ThTtsSynthesisHandler implements ThAudioProcessHandler {

    private final ThTtsService ttsService;
    private final ThTtsConfig ttsConfig;

    @Override
    public String getName() {
        return "并行TTS语音合成处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getScriptItems() != null && context.getTtsAudioPaths() == null;
    }

    @Override
    public int maxRetry() {
        return 3;
    }

    @Override
    public long retryDelayMs() {
        return 3000;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        List<ThAudioProcessContext.ScriptItem> items = context.getScriptItems();
        int concurrency = ttsConfig.getConcurrency();
        int total = items.size();

        log.info("[{}] 开始并行TTS合成, 共 {} 个条目, 并发度={}", getName(), total, concurrency);

        Semaphore semaphore = new Semaphore(concurrency);
        ExecutorService executor = Executors.newFixedThreadPool(concurrency);
        String[] audioPaths = new String[total];
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

                            String audioPath = ttsService.synthesizeToFile(
                                    item.getText(), item.getRole());
                            audioPaths[idx] = audioPath;

                            int done = completed.incrementAndGet();
                            // 上报进度（10%-80%区间）
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
                        throw new RuntimeException(
                                String.format("TTS合成失败 [%d] role=%s: %s",
                                        idx, item.getRole(), e.getMessage()), e);
                    }
                }, executor));
            }

            // 等待全部完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
        }

        context.setTtsAudioPaths(Arrays.asList(audioPaths));
        log.info("[{}] TTS合成完成, 共 {} 个音频片段", getName(), total);

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 80, "TTS合成完成");
        }
    }
}
