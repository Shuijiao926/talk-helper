package com.roamingguide.starter.otel;

import com.roamingguide.starter.tts.TtsService.TtsSynthesisResult;
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
 * TTS 语音合成链路追踪切面
 * 拦截 TtsService 的合成方法，记录 TTS 相关的观测数据
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class TtsTraceAspect {

    private final Tracer thTracer;

    /**
     * 拦截 TtsService.synthesizeToFileAndBytes() -- TTS 核心合成方法
     */
    @Around("execution(* com.roamingguide.starter.tts.TtsService.synthesizeToFileAndBytes(..))")
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

            if (result instanceof TtsSynthesisResult synthesisResult) {
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
}
