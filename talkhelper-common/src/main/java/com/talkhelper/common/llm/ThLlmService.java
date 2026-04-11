package com.talkhelper.common.llm;

import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * LLM服务层（模板方法模式）
 * 封装LLM调用的通用流程：渲染模板 → 调用模型 → 处理响应
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThLlmService {

    private final ThLlmClientFactory llmClientFactory;

    /**
     * 执行LLM调用（完整流程）
     *
     * @param request LLM请求
     * @return LLM响应
     */
    public ThLlmResponse execute(ThLlmRequest request) {
        log.info("开始LLM调用, 提供商: {}", llmClientFactory.getActiveClient().getType());

        // 1. 检查客户端可用性
        if (!llmClientFactory.getActiveClient().isAvailable()) {
            log.warn("LLM客户端不可用");
            return ThLlmResponse.failure("LLM服务不可用");
        }

        // 2. 渲染Prompt模板
        String finalPrompt = renderPrompt(request);
        request.setFinalPrompt(finalPrompt);

        log.debug("最终Prompt长度: {}", finalPrompt.length());

        // 3. 调用LLM客户端
        ThLlmResponse response = llmClientFactory.getActiveClient().chat(request);

        // 4. 记录日志
        if (response.isSuccess()) {
            log.info("LLM调用成功, 输出长度: {}, Token使用: {}", 
                    response.getContent() != null ? response.getContent().length() : 0,
                    response.getTotalTokens());
        } else {
            log.error("LLM调用失败: {}", response.getErrorMessage());
        }

        return response;
    }

    /**
     * 渲染Prompt模板
     */
    private String renderPrompt(ThLlmRequest request) {
        if (request.getFinalPrompt() != null) {
            // 如果已经提供了最终Prompt，直接返回
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
     *
     * @param prompt 提示词
     * @return 响应内容
     */
    public String quickChat(String prompt) {
        ThLlmRequest request = ThLlmRequest.builder()
                .finalPrompt(prompt)
                .build();

        ThLlmResponse response = execute(request);
        return response.isSuccess() ? response.getContent() : null;
    }
}
