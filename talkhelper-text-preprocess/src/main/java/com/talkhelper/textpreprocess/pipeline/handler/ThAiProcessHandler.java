package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThLogConstants;
import com.roamingguide.starter.llm.LlmRequest;
import com.roamingguide.starter.llm.LlmResponse;
import com.roamingguide.starter.llm.LlmService;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.service.ThPodcastPromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class ThAiProcessHandler implements ThTextProcessHandler {

    private final LlmService llmService;
    private final ThPodcastPromptTemplateService promptTemplateService;
    private final ExecutorService ioIntensiveExecutor;

    private static final int MAX_CONCURRENT_LLM_CALLS = 5;
    private static final Semaphore llmSemaphore = new Semaphore(MAX_CONCURRENT_LLM_CALLS);

    @Override
    public String getName() {
        return "AI处理处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        return context.getChunks() != null && !context.getChunks().isEmpty();
    }

    @Override
    public int maxRetry() {
        return 2;
    }

    @Override
    public long retryDelayMs() {
        return 2000;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始AI处理, 分块数量: {}", getName(), context.getChunks().size());

        try {
            String templateType = context.getAttribute("templateType");
            if (templateType == null) {
                templateType = "standard";
            }
            String promptTemplate = loadPromptTemplate(templateType);

            int totalChunks = context.getChunks().size();
            log.info("[{}] 启动并行处理, 总分块数: {}, 最大并发数: {}", getName(), totalChunks, MAX_CONCURRENT_LLM_CALLS);

            List<CompletableFuture<ChunkResult>> futures = IntStream.range(0, totalChunks)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            llmSemaphore.acquire();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return new ChunkResult(i, "[分块" + (i + 1) + "被中断]", false, e.getMessage());
                        }

                        try {
                            ThTextProcessContext.TextChunk chunk = context.getChunks().get(i);

                            log.info("[{}] 并行处理第 {}/{} 个分块, 长度: {}",
                                    getName(), i + 1, totalChunks, chunk.getContent().length());

                            Map<String, String> variables = new HashMap<>();
                            variables.put("input_text", chunk.getContent());
                            variables.put("minimum_words", "500");

                            LlmRequest request = LlmRequest.builder()
                                    .promptTemplate(promptTemplate)
                                    .variables(variables)
                                    .build();

                            LlmResponse response = llmService.execute(request);

                            if (!response.isSuccess()) {
                                log.warn("[{}] 分块 {}/{} 处理失败: {}",
                                        getName(), i + 1, totalChunks, response.getErrorMessage());
                                return new ChunkResult(i, "[分块" + (i + 1) + "处理失败]", false, response.getErrorMessage());
                            } else {
                                log.info("[{}] 分块 {}/{} 处理成功, 输出长度: {}",
                                        getName(), i + 1, totalChunks, response.getContent().length());
                                return new ChunkResult(i, response.getContent(), true, null);
                            }
                        } catch (Exception e) {
                            log.error("[{}] 分块 {}/{} 处理异常: {}", getName(), i + 1, totalChunks, e.getMessage(), e);
                            return new ChunkResult(i, "[分块" + (i + 1) + "处理异常: " + e.getMessage() + "]", false, e.getMessage());
                        } finally {
                            llmSemaphore.release();
                        }
                    }, ioIntensiveExecutor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<ChunkResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .sorted((r1, r2) -> Integer.compare(r1.index, r2.index))
                    .toList();

            StringBuilder mergedResult = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                ChunkResult result = results.get(i);
                mergedResult.append(result.content);

                if (i < results.size() - 1) {
                    mergedResult.append("\n\n--- 分块分隔 ---\n\n");
                }
            }

            context.setAiResult(mergedResult.toString());

            long successCount = results.stream().filter(r -> r.success).count();
            long failCount = results.stream().filter(r -> !r.success).count();

            log.info("[{}] AI处理完成, 总分块数: {}, 成功: {}, 失败: {}, 总输出长度: {}",
                    getName(), totalChunks, successCount, failCount, mergedResult.length());

        } catch (Exception e) {
            log.error(ThLogConstants.BUSINESS_EXCEPTION, e);
            context.setAiResult("[AI处理异常] " + e.getMessage());
            throw e;
        }
    }

    private static class ChunkResult {
        final int index;
        final String content;
        final boolean success;
        final String error;

        ChunkResult(int index, String content, boolean success, String error) {
            this.index = index;
            this.content = content;
            this.success = success;
            this.error = error;
        }
    }

    private String loadPromptTemplate(String templateType) {
        return switch (templateType.toLowerCase()) {
            case "deep_dive" -> promptTemplateService.getDeepDiveTemplate();
            case "quick_tips" -> promptTemplateService.getQuickTipsTemplate();
            case "storytelling" -> promptTemplateService.getStorytellingTemplate();
            case "debate" -> promptTemplateService.getDebateTemplate();
            default -> promptTemplateService.getStandardTemplate();
        };
    }
}
