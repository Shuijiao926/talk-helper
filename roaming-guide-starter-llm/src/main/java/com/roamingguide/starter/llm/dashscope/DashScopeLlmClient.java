package com.roamingguide.starter.llm.dashscope;

import com.roamingguide.starter.llm.LlmClient;
import com.roamingguide.starter.llm.LlmRequest;
import com.roamingguide.starter.llm.LlmResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
public class DashScopeLlmClient implements LlmClient {

    private final DashScopeApi dashScopeApi;

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
    public LlmResponse chat(LlmRequest request) {
        try {
            log.debug("调用DashScope模型, Prompt长度: {}",
                    request.getFinalPrompt() != null ? request.getFinalPrompt().length() : 0);

            DashScopeRequest apiRequest = buildRequest(request);
            DashScopeResponse apiResponse = dashScopeApi.chatCompletion(
                    "Bearer " + apiKey, apiRequest);

            if (apiResponse == null || apiResponse.getChoices() == null || apiResponse.getChoices().isEmpty()) {
                log.warn("DashScope返回结果为空");
                return LlmResponse.failure("AI返回结果为空");
            }

            DashScopeResponse.Choice choice = apiResponse.getChoices().get(0);
            String content = choice.getMessage() != null ? choice.getMessage().getContent() : null;

            if (content == null || content.isEmpty()) {
                log.warn("DashScope返回内容为空");
                return LlmResponse.failure("AI返回内容为空");
            }

            available.set(true);

            Integer promptTokens = null;
            Integer completionTokens = null;
            if (apiResponse.getUsage() != null) {
                promptTokens = apiResponse.getUsage().getPromptTokens();
                completionTokens = apiResponse.getUsage().getCompletionTokens();
            }

            return LlmResponse.success(
                    content,
                    apiResponse.getModel() != null ? apiResponse.getModel() : defaultModel,
                    promptTokens,
                    completionTokens
            );

        } catch (Exception e) {
            log.error("DashScope调用失败", e);
            available.set(false);
            lastCheckTime.set(Instant.now().toEpochMilli());
            return LlmResponse.failure("DashScope调用失败: " + e.getMessage());
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
            DashScopeRequest testRequest = DashScopeRequest.builder()
                    .model(defaultModel)
                    .messages(List.of(
                            DashScopeRequest.Message.builder()
                                    .role("user")
                                    .content("hi")
                                    .build()
                    ))
                    .maxTokens(10)
                    .build();
            DashScopeResponse testResponse = dashScopeApi.chatCompletion("Bearer " + apiKey, testRequest);
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

    private DashScopeRequest buildRequest(LlmRequest request) {
        String model = request.getModel() != null ? request.getModel() : defaultModel;
        Double temperature = request.getTemperature() != null ? request.getTemperature() : defaultTemperature;
        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : defaultMaxTokens;

        return DashScopeRequest.builder()
                .model(model)
                .messages(List.of(
                        DashScopeRequest.Message.builder()
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
