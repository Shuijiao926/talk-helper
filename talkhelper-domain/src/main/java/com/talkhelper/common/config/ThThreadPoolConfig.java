package com.talkhelper.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class ThThreadPoolConfig {

    @Bean("cpuIntensiveExecutor")
    public ExecutorService cpuIntensiveExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;
        AtomicInteger count = new AtomicInteger(0);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("CPU-Worker-" + count.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        log.info("CPU密集型线程池初始化完成: core={}, max={}", corePoolSize, maxPoolSize);
        return executor;
    }

    @Bean("ioIntensiveExecutor")
    public ExecutorService ioIntensiveExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = corePoolSize * 4;
        AtomicInteger count = new AtomicInteger(0);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("IO-Worker-" + count.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        log.info("IO密集型线程池初始化完成: core={}, max={}", corePoolSize, maxPoolSize);
        return executor;
    }

    @Bean("scheduledExecutor")
    public ScheduledExecutorService scheduledExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                5,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("Scheduled-Worker");
                    thread.setDaemon(true);
                    return thread;
                }
        );

        log.info("定时任务线程池初始化完成: size=5");
        return executor;
    }
}
