package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;
import com.talkhelper.common.llm.ThLlmService;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.service.ThPodcastPromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AI处理处理器
 * 使用通用LLM服务，将文本转换为播客脚本
 * 支持并行处理多个分块，提升性能
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThAiProcessHandler implements ThTextProcessHandler {

    private final ThLlmService llmService; // 通用LLM服务
    private final ThPodcastPromptTemplateService promptTemplateService; // 提示词模板服务
    private final ExecutorService ioIntensiveExecutor; // IO密集型线程池(用于LLM调用)

    @Override
    public String getName() {
        return "AI处理处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        // 如果有分块且需要AI处理，则执行
        return context.getChunks() != null && !context.getChunks().isEmpty();
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始AI处理, 分块数量: {}", getName(), context.getChunks().size());

        try {
            // 1. 获取提示词模板
            String templateType = context.getAttribute("templateType");
            if (templateType == null) {
                templateType = "standard";
            }
            String promptTemplate = loadPromptTemplate(templateType);

            int totalChunks = context.getChunks().size();
            
            // 2. 并行处理所有分块（使用IO线程池，因为LLM调用是IO密集型）
            log.info("[{}] 启动并行处理, 总分块数: {}", getName(), totalChunks);
            
            List<CompletableFuture<ChunkResult>> futures = IntStream.range(0, totalChunks)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        try {
                            ThTextProcessContext.TextChunk chunk = context.getChunks().get(i);
                            
                            log.info("[{}] 并行处理第 {}/{} 个分块, 长度: {}", 
                                    getName(), i + 1, totalChunks, chunk.getContent().length());

                            // 3. 构建LLM请求（每个分块独立）
                            Map<String, String> variables = new HashMap<>();
                            variables.put("input_text", chunk.getContent());

                            ThLlmRequest request = ThLlmRequest.builder()
                                    .promptTemplate(promptTemplate)
                                    .variables(variables)
                                    .build();

                            // 4. 执行LLM调用
                            ThLlmResponse response = llmService.execute(request);

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
                        }
                    }, ioIntensiveExecutor))  // 使用IO线程池
                    .collect(Collectors.toList());

            // 5. 等待所有任务完成（设置超时时间：每个分块最多60秒）
            try {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .get(60 * totalChunks, java.util.concurrent.TimeUnit.SECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                log.error("[{}] AI处理超时，总分块数: {}", getName(), totalChunks, e);
                throw new RuntimeException("AI处理超时: " + e.getMessage(), e);
            }

            // 6. 收集结果并按顺序合并
            List<ChunkResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .sorted((r1, r2) -> Integer.compare(r1.index, r2.index))
                    .toList();

            StringBuilder mergedResult = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                ChunkResult result = results.get(i);
                mergedResult.append(result.content);
                
                // 如果不是最后一个分块，添加分隔符
                if (i < results.size() - 1) {
                    mergedResult.append("\n\n--- 分块分隔 ---\n\n");
                }
            }

            context.setAiResult(mergedResult.toString());
            
            // 统计成功和失败数量
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

    /**
     * 分块处理结果
     */
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

    /**
     * 加载提示词模板
     */
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
