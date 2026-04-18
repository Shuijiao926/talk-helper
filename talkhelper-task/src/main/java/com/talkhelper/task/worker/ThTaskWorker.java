package com.talkhelper.task.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.audio.service.ThAudioProcessService;
import com.talkhelper.task.mq.ThMessage;
import com.talkhelper.task.mq.ThMessageQueue;
import com.talkhelper.task.mq.ThMessageQueueFactory;
import com.talkhelper.task.mq.ThRedisMessageQueue;
import com.talkhelper.task.service.ThAsyncTaskService;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 任务Worker - 消费消息队列中的任务
 * 
 * 架构设计：专用消费线程 + CPU线程池任务分发 + ACK确认
 * - 2个专用消费线程负责从Redis Stream XREADGROUP取任务（I/O密集）
 * - 取到任务后提交到CPU线程池执行实际处理（CPU密集）
 * - XREADGROUP使用30秒超时，无任务时线程阻塞在Redis端，零CPU开销
 * - 任务处理完成后通过XACK确认，防止消息丢失
 * - Redis不可用时5秒间隔健康检查，自动恢复消费
 */
@Slf4j
@Component
public class ThTaskWorker implements CommandLineRunner {

    private final ThAsyncTaskService taskService;
    private final ThTextPreprocessService preprocessService;
    private final ThAudioProcessService audioProcessService;
    private final ObjectMapper objectMapper;
    private final ThMessageQueueFactory mqFactory;
    private final ExecutorService cpuIntensiveExecutor;
    private final RedisConnectionFactory redisConnectionFactory;

    private static final int CONSUMER_THREAD_COUNT = 2;
    private static final long READ_TIMEOUT_SECONDS = 30;
    private static final long RECOVERY_CHECK_INTERVAL_MS = 5000;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Thread[] consumerThreads;

    public ThTaskWorker(
            ThAsyncTaskService taskService,
            ThTextPreprocessService preprocessService,
            ThAudioProcessService audioProcessService,
            ObjectMapper objectMapper,
            ThMessageQueueFactory mqFactory,
            @Qualifier("cpuIntensiveExecutor") ExecutorService cpuIntensiveExecutor,
            RedisConnectionFactory redisConnectionFactory) {
        this.taskService = taskService;
        this.preprocessService = preprocessService;
        this.audioProcessService = audioProcessService;
        this.objectMapper = objectMapper;
        this.mqFactory = mqFactory;
        this.cpuIntensiveExecutor = cpuIntensiveExecutor;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void run(String... args) {
        log.info("========== 任务Worker启动 ==========");

        consumerThreads = new Thread[CONSUMER_THREAD_COUNT];
        for (int i = 0; i < CONSUMER_THREAD_COUNT; i++) {
            consumerThreads[i] = new Thread(this::consumeTasks, "Redis-Consumer-" + (i + 1));
            consumerThreads[i].setDaemon(true);
            consumerThreads[i].start();
        }

        log.info("已启动 {} 个专用消费线程，XREADGROUP超时: {}s", CONSUMER_THREAD_COUNT, READ_TIMEOUT_SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        log.info("========== 任务Worker开始优雅停机 ==========");
        running.set(false);

        if (consumerThreads != null) {
            for (Thread t : consumerThreads) {
                if (t != null) {
                    t.interrupt();
                }
            }
        }

        log.info("任务Worker已停止");
    }

    private void consumeTasks() {
        String threadName = Thread.currentThread().getName();
        log.info("消费线程 [{}] 启动", threadName);

        while (running.get() && !Thread.currentThread().isInterrupted()) {
            try {
                ThMessageQueue mq = mqFactory.getActiveMQ();

                if (!mq.isAvailable()) {
                    waitForRecovery(threadName);
                    continue;
                }

                ThMessage message = mq.receiveTask(READ_TIMEOUT_SECONDS);

                if (message == null) {
                    continue;
                }

                log.info("消费线程 [{}] 收到任务: {}, deliveryId={}, 提交到CPU线程池处理",
                        threadName, message.getTaskId(), message.getDeliveryId());
                cpuIntensiveExecutor.submit(() -> processTask(message));

            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    log.info("消费线程 [{}] 被中断，退出", threadName);
                    break;
                }
                log.error("消费线程 [{}] 异常", threadName, e);
                sleepSafely(1000);
            }
        }

        log.info("消费线程 [{}] 已停止", threadName);
    }

    private void waitForRecovery(String threadName) {
        ThMessageQueue mq = mqFactory.getActiveMQ();
        int checkCount = 0;

        while (running.get() && !mq.isAvailable()) {
            checkCount++;
            if (checkCount % 12 == 1) {
                log.warn("消费线程 [{}] 等待Redis恢复... (已等待约 {}s)",
                        threadName, checkCount * RECOVERY_CHECK_INTERVAL_MS / 1000);
            }

            sleepSafely(RECOVERY_CHECK_INTERVAL_MS);

            if (mq instanceof ThRedisMessageQueue redisMQ) {
                try {
                    if (pingCheck()) {
                        redisMQ.markAvailable();
                        log.info("消费线程 [{}] 检测到Redis已恢复", threadName);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private boolean pingCheck() {
        try {
            redisConnectionFactory.getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 处理单个任务 - 在CPU线程池中执行
     * 处理完成后通过ACK确认消息，确保至少一次消费语义
     */
    private void processTask(ThMessage message) {
        String taskId = message.getTaskId();
        String deliveryId = message.getDeliveryId();
        String threadName = Thread.currentThread().getName();

        try {
            log.info("处理线程 [{}] 开始处理任务: {}, deliveryId={}", threadName, taskId, deliveryId);

            taskService.startTask(taskId);

            var task = taskService.getTask(taskId);
            if (task == null) {
                taskService.failTask(taskId, "任务不存在");
                mqFactory.getActiveMQ().ackTask(deliveryId);
                return;
            }

            if ("cancelled".equals(task.getStatus())) {
                log.info("任务已取消，跳过执行: {}", taskId);
                mqFactory.getActiveMQ().ackTask(deliveryId);
                return;
            }

            String requestData = taskService.getRequestData(taskId);
            if (requestData == null) {
                taskService.failTask(taskId, "请求数据不存在");
                mqFactory.getActiveMQ().ackTask(deliveryId);
                return;
            }

            ThFileUploadRequest request = objectMapper.readValue(
                    requestData,
                    ThFileUploadRequest.class
            );
            request.setTaskId(taskId);

            taskService.updateProgress(taskId, 10, "正在解析文档");

            var result = preprocessService.uploadAndProcessWithConfig(request);

            // ===== 阶段2：音频生成（文本预处理产出播客脚本后，TTS合成音频） =====
            String podcastScript = result.getAiResult();
            String audioUrl = null;
            if (podcastScript != null && !podcastScript.isEmpty()) {
                taskService.updateProgress(taskId, 50, "正在生成播客音频");
                try {
                    audioUrl = audioProcessService.generatePodcastAudio(podcastScript, taskId);
                    log.info("播客音频生成完成: taskId={}, audioUrl={}", taskId, audioUrl);
                } catch (Exception e) {
                    log.error("音频生成失败，任务继续完成（仅文本结果）: taskId={}", taskId, e);
                }
            }

            taskService.updateProgress(taskId, 95, "正在生成结果");

            if (result.getOutputFileUrl() != null) {
                task.setOutputFileName(result.getOutputFileName());
                task.setOutputFileUrl(result.getOutputFileUrl());
                if (result.getAiResult() != null) {
                    task.setOutputFileSize((long) result.getAiResult().getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
                }
                log.info("任务输出文件信息: name={}, url={}", result.getOutputFileName(), result.getOutputFileUrl());
            }

            String resultJson = objectMapper.writeValueAsString(result);
            taskService.completeTask(taskId, resultJson);

            mqFactory.getActiveMQ().ackTask(deliveryId);
            log.info("任务处理完成并已ACK: taskId={}, deliveryId={}", taskId, deliveryId);

        } catch (Exception e) {
            log.error("任务处理失败: taskId={}, deliveryId={}", taskId, deliveryId, e);
            taskService.failTask(taskId, e.getMessage());
            mqFactory.getActiveMQ().ackTask(deliveryId);
        }
    }
}
