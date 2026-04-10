package com.talkhelper.web.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Prometheus自定义监控指标配置
 */
@Slf4j
@Configuration
public class ThMetricsConfig {

    private final MeterRegistry meterRegistry;

    // 自定义指标
    private Counter taskCreatedCounter;
    private Counter taskCompletedCounter;
    private Counter taskFailedCounter;
    private Timer taskProcessingTimer;
    private AtomicLong activeTaskCount;

    public ThMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void initMetrics() {
        // 任务创建计数器
        taskCreatedCounter = Counter.builder("talkhelper.task.created")
                .description("任务创建总数")
                .tag("application", "talkhelper")
                .register(meterRegistry);

        // 任务完成计数器
        taskCompletedCounter = Counter.builder("talkhelper.task.completed")
                .description("任务完成总数")
                .tag("application", "talkhelper")
                .register(meterRegistry);

        // 任务失败计数器
        taskFailedCounter = Counter.builder("talkhelper.task.failed")
                .description("任务失败总数")
                .tag("application", "talkhelper")
                .register(meterRegistry);

        // 任务处理耗时
        taskProcessingTimer = Timer.builder("talkhelper.task.processing.time")
                .description("任务处理耗时")
                .tag("application", "talkhelper")
                .register(meterRegistry);

        // 活跃任务数
        activeTaskCount = meterRegistry.gauge("talkhelper.task.active",
                new AtomicLong(0));

        log.info("Prometheus自定义指标初始化完成");
    }

    /**
     * 记录任务创建
     */
    public void recordTaskCreated() {
        taskCreatedCounter.increment();
        activeTaskCount.incrementAndGet();
    }

    /**
     * 记录任务完成
     */
    public void recordTaskCompleted(long durationMs) {
        taskCompletedCounter.increment();
        taskProcessingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        activeTaskCount.decrementAndGet();
    }

    /**
     * 记录任务失败
     */
    public void recordTaskFailed() {
        taskFailedCounter.increment();
        activeTaskCount.decrementAndGet();
    }

    /**
     * 获取当前活跃任务数
     */
    public long getActiveTaskCount() {
        return activeTaskCount.get();
    }
}
