package com.talkhelper.common.llm;

import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;
import com.talkhelper.common.llm.dashscope.ThDashScopeApi;
import com.talkhelper.common.llm.dashscope.ThDashScopeRequest;
import com.talkhelper.common.llm.dashscope.ThDashScopeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.ai.dashscope.api-key")
public class ThDashScopeLlmClient implements ThLlmClient {

    private final ThDashScopeApi dashScopeApi;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}")
    private String defaultModel;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.7}")
    private Double defaultTemperature;

    @Value("${spring.ai.dashscope.chat.options.max-tokens:4096}")
    private Integer defaultMaxTokens;

    private final AtomicBoolean available = new AtomicBoolean(true);
    private final AtomicLong lastCheckTime = new AtomicLong(0);
    private static final long CHECK_INTERVAL_MS = 60_000;

    @Override
    public String getType() {
        return "dashscope";
    }

    @Override
    public ThLlmResponse chat(ThLlmRequest request) {
        try {
            log.debug("调用DashScope模型, Prompt长度: {}",
                    request.getFinalPrompt() != null ? request.getFinalPrompt().length() : 0);

            ThDashScopeRequest apiRequest = buildRequest(request);
            ThDashScopeResponse apiResponse = dashScopeApi.chatCompletion(
                    "Bearer " + apiKey, apiRequest);

            if (apiResponse == null || apiResponse.getChoices() == null || apiResponse.getChoices().isEmpty()) {
                log.warn("DashScope返回结果为空");
                return ThLlmResponse.failure("AI返回结果为空");
            }

            ThDashScopeResponse.Choice choice = apiResponse.getChoices().get(0);
            String content = choice.getMessage() != null ? choice.getMessage().getContent() : null;

            if (content == null || content.isEmpty()) {
                log.warn("DashScope返回内容为空");
                return ThLlmResponse.failure("AI返回内容为空");
            }

            available.set(true);

            Integer promptTokens = null;
            Integer completionTokens = null;
            if (apiResponse.getUsage() != null) {
                promptTokens = apiResponse.getUsage().getPromptTokens();
                completionTokens = apiResponse.getUsage().getCompletionTokens();
            }

            return ThLlmResponse.success(
                    content,
                    apiResponse.getModel() != null ? apiResponse.getModel() : defaultModel,
                    promptTokens,
                    completionTokens
            );

        } catch (Exception e) {
            log.error("DashScope调用失败", e);
            available.set(false);
            lastCheckTime.set(Instant.now().toEpochMilli());
            return ThLlmResponse.failure("DashScope调用失败: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        if (available.get()) {
            return true;
        }
        long elapsed = Instant.now().toEpochMilli() - lastCheckTime.get();
        if (elapsed < CHECK_INTERVAL_MS) {
            return false;
        }
        try {
            ThDashScopeRequest testRequest = ThDashScopeRequest.builder()
                    .model(defaultModel)
                    .messages(List.of(
                            ThDashScopeRequest.Message.builder()
                                    .role("user")
                                    .content("hi")
                                    .build()
                    ))
                    .maxTokens(10)
                    .build();
            ThDashScopeResponse testResponse = dashScopeApi.chatCompletion("Bearer " + apiKey, testRequest);
            boolean ok = testResponse != null
                    && testResponse.getChoices() != null
                    && !testResponse.getChoices().isEmpty();
            available.set(ok);
            lastCheckTime.set(Instant.now().toEpochMilli());
            return ok;
        } catch (Exception e) {
            log.debug("DashScope不可用: {}", e.getMessage());
            lastCheckTime.set(Instant.now().toEpochMilli());
            return false;
        }
    }

    @Override
    public void shutdown() {
        log.info("DashScope LLM客户端关闭");
    }

    private ThDashScopeRequest buildRequest(ThLlmRequest request) {
        String model = request.getModel() != null ? request.getModel() : defaultModel;
        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens;

        return ThDashScopeRequest.builder()
                .model(model)
                .messages(List.of(
                        ThDashScopeRequest.Message.builder()
                                .role("user")
                                .content(request.getFinalPrompt())
                                .build()
                ))
                .temperature(temperature)
                .maxTokens(maxTokens)
                .stream(false)
                .build();
    }
}
