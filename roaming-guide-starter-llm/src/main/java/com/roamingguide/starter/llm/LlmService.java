package com.roamingguide.starter.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * LLM 服务层（模板方法模式）
 * 封装 LLM 调用的通用流程：渲染模板 -> 调用模型 -> 处理响应
 */
@Slf4j
@RequiredArgsConstructor
public class LlmService {

    private final LlmClientFactory llmClientFactory;

    /**
     * 执行 LLM 调用（完整流程）
     */
    public LlmResponse execute(LlmRequest request) {
        log.info("开始LLM调用, 提供商: {}", llmClientFactory.getActiveClient().getType());

        if (!llmClientFactory.getActiveClient().isAvailable()) {
            log.warn("LLM客户端不可用");
            return LlmResponse.failure("LLM服务不可用");
        }

        String finalPrompt = renderPrompt(request);
        request.setFinalPrompt(finalPrompt);

        log.debug("最终Prompt长度: {}", finalPrompt.length());

        LlmResponse response = llmClientFactory.getActiveClient().chat(request);

        if (response.isSuccess()) {
            log.info("LLM调用成功, 输出长度: {}, Token使用: {}",
                    response.getContent() != null ? response.getContent().length() : 0,
                    response.getTotalTokens());
        } else {
            log.error("LLM调用失败: {}", response.getErrorMessage());
        }

        return response;
    }

    private String renderPrompt(LlmRequest request) {
        if (request.getFinalPrompt() != null) {
            return request.getFinalPrompt();
        }

        if (request.getPromptTemplate() == null || request.getVariables() == null) {
            throw new IllegalArgumentException("Prompt模板和变量不能为空");
        }

        String result = request.getPromptTemplate();
        for (Map.Entry<String, String> entry : request.getVariables().entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }

        return result;
    }

    /**
     * 快速调用（简化版）
     */
    public String quickChat(String prompt) {
        LlmRequest request = LlmRequest.builder()
                .finalPrompt(prompt)
                .build();

        LlmResponse response = execute(request);
        return response.isSuccess() ? response.getContent() : null;
    }
}
