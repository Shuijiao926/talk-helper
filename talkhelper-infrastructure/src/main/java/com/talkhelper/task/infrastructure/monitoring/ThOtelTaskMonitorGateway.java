package com.talkhelper.task.infrastructure.monitoring;

import com.roamingguide.starter.otel.AiSemanticAttributes;
import com.talkhelper.task.domain.monitoring.ThTaskMonitorGateway;
import com.talkhelper.task.domain.monitoring.ThTaskMonitoringOperation;
import com.talkhelper.task.domain.monitoring.ThTaskTraceScope;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThOtelTaskMonitorGateway implements ThTaskMonitorGateway {

    private final Tracer thTracer;

    @Override
    public void traceTaskProcessing(String taskId, ThTaskMonitoringOperation operation) {
        Span span = thTracer.spanBuilder(AiSemanticAttributes.SPAN_TASK_PROCESS)
                .setAttribute(AiSemanticAttributes.TASK_ID, taskId)
                .setAttribute(AiSemanticAttributes.AGENT_NAME, "talk-helper")
                .setAttribute(AiSemanticAttributes.AGENT_ACTION, "podcast-generate")
                .startSpan();

        try (Scope ignored = span.makeCurrent()) {
            operation.run(new OtelTraceScope(span));
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            span.setAttribute(AiSemanticAttributes.TASK_STATUS, "failed");
            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }

    private static final class OtelTraceScope implements ThTaskTraceScope {

        private final Span span;

        private OtelTraceScope(Span span) {
            this.span = span;
        }

        @Override
        public void markStatus(String status) {
            span.setAttribute(AiSemanticAttributes.TASK_STATUS, status);
        }

        @Override
        public void addAttribute(String key, String value) {
            span.setAttribute(key, value);
        }

        @Override
        public void fail(String message, Throwable throwable) {
            span.setStatus(StatusCode.ERROR, message);
            span.setAttribute(AiSemanticAttributes.TASK_STATUS, "failed");
            if (throwable != null) {
                span.recordException(throwable);
            }
        }
    }
}
