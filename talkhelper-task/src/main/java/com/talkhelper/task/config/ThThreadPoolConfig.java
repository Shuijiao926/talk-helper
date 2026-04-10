package com.talkhelper.task.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 线程池配置 - CPU密集型和IO密集型分离
 */
@Slf4j
@Configuration
public class ThThreadPoolConfig {

    /**
     * CPU密集型线程池
     * 用于：文档解析、文本清洗、AI计算等CPU密集操作
     * 核心线程数 = CPU核数
     */
    @Bean("cpuIntensiveExecutor")
    public ExecutorService cpuIntensiveExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        int maxPoolSize = corePoolSize * 2;
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("CPU-Worker-" + (++count));
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 背压策略：队列满时由调用线程执行
        );
        
        log.info("CPU密集型线程池初始化完成: core={}, max={}", corePoolSize, maxPoolSize);
        return executor;
    }

    /**
     * IO密集型线程池
     * 用于：文件上传下载、数据库操作、API调用等IO密集操作
     * 核心线程数 = CPU核数 * 2
     */
    @Bean("ioIntensiveExecutor")
    public ExecutorService ioIntensiveExecutor() {
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        int maxPoolSize = corePoolSize * 4;
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("IO-Worker-" + (++count));
                        thread.setDaemon(true);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.AbortPolicy() // 拒绝策略：直接抛出异常
        );
        
        log.info("IO密集型线程池初始化完成: core={}, max={}", corePoolSize, maxPoolSize);
        return executor;
    }

    /**
     * 定时任务线程池
     * 用于：定时清理过期任务、统计信息等
     */
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
