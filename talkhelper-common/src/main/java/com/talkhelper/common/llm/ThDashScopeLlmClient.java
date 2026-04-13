package com.talkhelper.common.llm;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 阿里云通义千问（DashScope）LLM客户端
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.ai.dashscope.api-key")
public class ThDashScopeLlmClient implements ThLlmClient {

    private final DashScopeChatModel chatModel;

    @Override
    public String getType() {
        return "dashscope";
    }

    @Override
    public ThLlmResponse chat(ThLlmRequest request) {
        try {
            log.debug("调用DashScope模型, Prompt长度: {}", 
                    request.getFinalPrompt() != null ? request.getFinalPrompt().length() : 0);

            // 调用Spring AI Alibaba
            String response = chatModel.call(request.getFinalPrompt());

            if (response == null || response.isEmpty()) {
                log.warn("DashScope返回结果为空");
                return ThLlmResponse.failure("AI返回结果为空");
            }

            // TODO: Spring AI Alibaba目前版本无法直接获取Token使用情况
            // 后续版本更新后可补充
            return ThLlmResponse.success(
                    response,
                    "qwen-plus", // 默认模型
                    null, // promptTokens
                    null  // completionTokens
            );

        } catch (Exception e) {
            log.error("DashScope调用失败", e);
            return ThLlmResponse.failure("DashScope调用失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // 简单测试：发送一个ping请求
            String testResponse = chatModel.call("你好");
            return testResponse != null && !testResponse.isEmpty();
        } catch (Exception e) {
            log.debug("DashScope不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void shutdown() {
        log.info("DashScope LLM客户端关闭");
    }
}
