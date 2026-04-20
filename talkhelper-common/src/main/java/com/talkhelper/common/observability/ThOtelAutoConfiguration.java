package com.talkhelper.common.observability;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry 自动配置
 * 当使用 OTel Java Agent 启动时，Agent 会自动注册 GlobalOpenTelemetry；
 * 当无 Agent 时（本地开发），使用 noop 实现，不影响正常运行。
 */
@Slf4j
@Configuration
@ConditionalOnClass(OpenTelemetry.class)
@EnableConfigurationProperties(ThObservabilityProperties.class)
public class ThOtelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry() {
        // OTel Java Agent 会自动设置 GlobalOpenTelemetry
        // 无 Agent 时返回 noop 实现
        OpenTelemetry otel = GlobalOpenTelemetry.get();
        log.info("OpenTelemetry 初始化完成, 实现类: {}", otel.getClass().getSimpleName());
        return otel;
    }

    @Bean
    @ConditionalOnMissingBean
    public Tracer thTracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("talkhelper", "1.0.0");
    }
}
