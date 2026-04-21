package com.roamingguide.starter.otel;

import com.roamingguide.starter.llm.LlmRequest;
import com.roamingguide.starter.llm.LlmResponse;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * LLM 调用链路追踪切面
 * 拦截 LlmService.execute() 方法，自动创建 Span 并记录 AI 语义属性：
 * - 模型名称、Token 用量、调用耗时
 * - 可选记录 Prompt/Completion 内容（受配置控制）
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class LlmTraceAspect {

    private final Tracer thTracer;
    private final ObservabilityProperties properties;

    /**
     * 拦截 LlmService.execute() 方法
     */
    @Around("execution(* com.roamingguide.starter.llm.LlmService.execute(..))")
    public Object traceLlmCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        LlmRequest request = (args.length > 0 && args[0] instanceof LlmRequest)
                ? (LlmRequest) args[0] : null;

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
            if (result instanceof LlmResponse response) {
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
