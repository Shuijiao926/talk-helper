package com.talkhelper.common.observability;

import com.talkhelper.common.tts.ThTtsService.ThTtsSynthesisResult;
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
 * TTS 语音合成链路追踪切面
 * 拦截 ThTtsService 的合成方法，记录 TTS 相关的观测数据：
 * - 模型名称、音色、输入文本长度
 * - 输出音频大小、格式
 * - 合成耗时
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "talkhelper.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThTtsTraceAspect {

    private final Tracer thTracer;

    /**
     * 拦截 ThTtsService.synthesizeToFileAndBytes() —— TTS 核心合成方法
     */
    @Around("execution(* com.talkhelper.common.tts.ThTtsService.synthesizeToFileAndBytes(..))")
    public Object traceTtsSynthesize(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String text = (args.length > 0 && args[0] instanceof String) ? (String) args[0] : "";
        String role = (args.length > 1 && args[1] instanceof String) ? (String) args[1] : "unknown";

        Span span = thTracer.spanBuilder(AiSemanticAttributes.SPAN_TTS_SYNTHESIZE)
                .setAttribute(AiSemanticAttributes.TTS_ROLE, role)
                .setAttribute(AiSemanticAttributes.TTS_INPUT_TEXT_LENGTH, (long) text.length())
                .setAttribute(AiSemanticAttributes.AGENT_NAME, "talk-helper")
                .startSpan();

        long startTime = System.currentTimeMillis();

        try (Scope ignored = span.makeCurrent()) {
            Object result = joinPoint.proceed();

            long durationMs = System.currentTimeMillis() - startTime;
            span.setAttribute(AiSemanticAttributes.TTS_DURATION_MS, durationMs);

            if (result instanceof ThTtsSynthesisResult synthesisResult) {
                if (synthesisResult.getAudioData() != null) {
                    span.setAttribute(AiSemanticAttributes.TTS_OUTPUT_AUDIO_BYTES,
                            (long) synthesisResult.getAudioData().length);
                }
                if (synthesisResult.getFormat() != null) {
                    span.setAttribute(AiSemanticAttributes.TTS_OUTPUT_FORMAT, synthesisResult.getFormat());
                }
            }

            return result;
        } catch (Throwable t) {
            long durationMs = System.currentTimeMillis() - startTime;
            span.setAttribute(AiSemanticAttributes.TTS_DURATION_MS, durationMs);
            span.setStatus(StatusCode.ERROR, t.getMessage());
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    /**
     * 拦截 ThTtsService.synthesizeToFile() —— 简化版合成（内部委托给 synthesizeToFileAndBytes）
     * 由于 synthesizeToFile 内部调用了 synthesizeToFileAndBytes，不需要重复追踪
     * 这里不做处理，避免 Span 嵌套重复
     */
}
