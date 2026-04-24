package com.talkhelper.common.observability;

import com.roamingguide.starter.otel.AiSemanticAttributes;
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
 * 任务处理流水线链路追踪切面
 * 拦截核心业务流水线的各阶段，形成完整的端到端调用链：
 *
 * task.process (TaskWorker.processTask)
 *   ├── task.text_preprocess (ThTextPreprocessService.uploadAndProcessWithConfig)
 *   │     └── llm.chat (LlmService.execute) -- 由 LlmTraceAspect 负责
 *   └── task.audio_gen (ThAudioProcessService.generatePodcastAudio)
 *         └── tts.synthesize (TtsService.synthesizeToFileAndBytes) -- 由 TtsTraceAspect 负责
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "roaming-guide.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ThTaskTraceAspect {

    private final Tracer thTracer;

    /**
     * 拦截文本预处理阶段
     * ThTextPreprocessService.uploadAndProcessWithConfig()
     */
    @Around("execution(* com.talkhelper.textpreprocess.service.ThTextPreprocessService.uploadAndProcessWithConfig(..))")
    public Object traceTextPreprocess(ProceedingJoinPoint joinPoint) throws Throwable {
        return traceStep(joinPoint,
                AiSemanticAttributes.SPAN_TASK_TEXT_PREPROCESS,
                "text-preprocess", 1);
    }

    /**
     * 拦截音频生成阶段
     * ThAudioProcessService.generatePodcastAudio()
     */
    @Around("execution(* com.talkhelper.audio.service.ThAudioProcessService.generatePodcastAudio(..))")
    public Object traceAudioGeneration(ProceedingJoinPoint joinPoint) throws Throwable {
        // 提取 taskId 参数
        Object[] args = joinPoint.getArgs();
        String taskId = (args.length > 1 && args[1] instanceof String) ? (String) args[1] : null;

        Span span = thTracer.spanBuilder(AiSemanticAttributes.SPAN_TASK_AUDIO_GEN)
                .setAttribute(AiSemanticAttributes.AGENT_NAME, "talk-helper")
                .setAttribute(AiSemanticAttributes.AGENT_WORKFLOW_STEP, 2L)
                .setAttribute(AiSemanticAttributes.AGENT_WORKFLOW_STEP_NAME, "audio-gen")
                .startSpan();

        if (taskId != null) {
            span.setAttribute(AiSemanticAttributes.TASK_ID, taskId);
        }

        // 记录播客脚本长度
        if (args.length > 0 && args[0] instanceof String script) {
            span.setAttribute("task.audio_gen.script_length", (long) script.length());
        }

        long startTime = System.currentTimeMillis();

        try (Scope ignored = span.makeCurrent()) {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;
            span.setAttribute("task.audio_gen.duration_ms", durationMs);

            if (result instanceof String audioUrl) {
                span.setAttribute("task.audio_gen.output_url", audioUrl);
            }
            return result;
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, t.getMessage());
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    /**
     * 通用步骤追踪
     */
    private Object traceStep(ProceedingJoinPoint joinPoint, String spanName,
                             String stepName, int stepOrder) throws Throwable {
        Span span = thTracer.spanBuilder(spanName)
                .setAttribute(AiSemanticAttributes.AGENT_NAME, "talk-helper")
                .setAttribute(AiSemanticAttributes.AGENT_WORKFLOW_STEP, (long) stepOrder)
                .setAttribute(AiSemanticAttributes.AGENT_WORKFLOW_STEP_NAME, stepName)
                .startSpan();

        long startTime = System.currentTimeMillis();

        try (Scope ignored = span.makeCurrent()) {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startTime;
            span.setAttribute("step.duration_ms", durationMs);
            return result;
        } catch (Throwable t) {
            span.setStatus(StatusCode.ERROR, t.getMessage());
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}
