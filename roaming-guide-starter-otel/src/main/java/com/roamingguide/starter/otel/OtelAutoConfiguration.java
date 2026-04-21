package com.roamingguide.starter.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * OpenTelemetry AI 可观测性自动配置
 * 当使用 OTel Java Agent 启动时，Agent 会自动注册 GlobalOpenTelemetry；
 * 当无 Agent 时（本地开发），使用 noop 实现，不影响正常运行。
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(OpenTelemetry.class)
@EnableConfigurationProperties(ObservabilityProperties.class)
public class OtelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry() {
        OpenTelemetry otel = GlobalOpenTelemetry.get();
        log.info("OpenTelemetry 初始化完成, 实现类: {}", otel.getClass().getSimpleName());
        return otel;
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer thTracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("roaming-guide", "1.0.0");
    }

    /**
     * LLM 链路追踪切面 -- 仅当 starter-llm 在 classpath 时加载
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.roamingguide.starter.llm.LlmService")
    @ConditionalOnProperty(prefix = "roaming-guide.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
    public LlmTraceAspect llmTraceAspect(Tracer thTracer, ObservabilityProperties properties) {
        return new LlmTraceAspect(thTracer, properties);
    }

    /**
     * TTS 链路追踪切面 -- 仅当 starter-tts 在 classpath 时加载
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.roamingguide.starter.tts.TtsService")
    @ConditionalOnProperty(prefix = "roaming-guide.observability", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TtsTraceAspect ttsTraceAspect(Tracer thTracer) {
        return new TtsTraceAspect(thTracer);
    }
}
