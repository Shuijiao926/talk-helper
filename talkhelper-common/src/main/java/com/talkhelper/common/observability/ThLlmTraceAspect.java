package com.talkhelper.common.observability;

import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * LLM 调用链路追踪切面
 * 拦截 ThLlmService.execute() 方法，自动创建 Span 并记录 AI 语义属性：
 * - 模型名称、Token 用量、调用耗时
 * - 可选记录 Prompt/Completion 内容（受配置控制）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "talkhelper.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThLlmTraceAspect {

    private final Tracer thTracer;
    private final ThObservabilityProperties properties;

    /**
     * 拦截 ThLlmService.execute() 方法
     */
    @Around("execution(* com.talkhelper.common.llm.ThLlmService.execute(..))")
    public Object traceLlmCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        ThLlmRequest request = (args.length > 0 && args[0] instanceof ThLlmRequest)
                ? (ThLlmRequest) args[0] : null;

        Span span = thTracer.spanBuilder(AiSemanticAttributes.SPAN_LLM_CHAT)
                .setAttribute(AiSemanticAttributes.GEN_AI_SYSTEM, "dashscope")
                .setAttribute(AiSemanticAttributes.AGENT_NAME, "talk-helper")
                .startSpan();

        // 记录请求参数
        if (request != null) {
            if (request.getModel() != null) {
                span.setAttribute(AiSemanticAttributes.GEN_AI_REQUEST_MODEL, request.getModel());
            }
            if (request.getTemperature() != null) {
                span.setAttribute(AiSemanticAttributes.GEN_AI_REQUEST_TEMPERATURE, request.getTemperature());
            }
            if (request.getMaxTokens() != null) {
                span.setAttribute(AiSemanticAttributes.GEN_AI_REQUEST_MAX_TOKENS, request.getMaxTokens());
            }
            if (properties.isLogPromptContent() && request.getFinalPrompt() != null) {
                span.setAttribute(AiSemanticAttributes.GEN_AI_PROMPT,
                        truncate(request.getFinalPrompt(), properties.getPromptMaxLength()));
            }
        }

        long startTime = System.currentTimeMillis();

        try (Scope ignored = span.makeCurrent()) {
            Object result = joinPoint.proceed();

            long durationMs = System.currentTimeMillis() - startTime;
            span.setAttribute(AiSemanticAttributes.GEN_AI_PERFORMANCE_DURATION_MS, durationMs);

            // 记录响应信息
            if (result instanceof ThLlmResponse response) {
                span.setAttribute(AiSemanticAttributes.GEN_AI_RESPONSE_SUCCESS, response.isSuccess());

                if (response.isSuccess()) {
                    if (response.getModel() != null) {
                        span.setAttribute(AiSemanticAttributes.GEN_AI_REQUEST_MODEL, response.getModel());
                    }
                    if (response.getPromptTokens() != null) {
                        span.setAttribute(AiSemanticAttributes.GEN_AI_USAGE_INPUT_TOKENS, response.getPromptTokens());
                    }
                    if (response.getCompletionTokens() != null) {
                        span.setAttribute(AiSemanticAttributes.GEN_AI_USAGE_OUTPUT_TOKENS, response.getCompletionTokens());
                    }
                    if (response.getTotalTokens() != null) {
                        span.setAttribute(AiSemanticAttributes.GEN_AI_USAGE_TOTAL_TOKENS, response.getTotalTokens());
                    }
                    if (properties.isLogCompletionContent() && response.getContent() != null) {
                        span.setAttribute(AiSemanticAttributes.GEN_AI_COMPLETION,
                                truncate(response.getContent(), properties.getCompletionMaxLength()));
                    }
                } else {
                    span.setStatus(StatusCode.ERROR, response.getErrorMessage());
                    span.setAttribute(AiSemanticAttributes.GEN_AI_RESPONSE_ERROR, response.getErrorMessage());
                }
            }

            return result;
        } catch (Throwable t) {
            long durationMs = System.currentTimeMillis() - startTime;
            span.setAttribute(AiSemanticAttributes.GEN_AI_PERFORMANCE_DURATION_MS, durationMs);
            span.setStatus(StatusCode.ERROR, t.getMessage());
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    private String truncate(String content, int maxLength) {
        if (content == null) return null;
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...(truncated)";
    }
}
