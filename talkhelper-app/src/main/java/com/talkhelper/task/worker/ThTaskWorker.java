package com.talkhelper.task.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.audio.service.ThAudioProcessService;
import com.talkhelper.task.domain.monitoring.ThTaskMonitorGateway;
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
    private final ThTaskMonitorGateway taskMonitorGateway;

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
            RedisConnectionFactory redisConnectionFactory,
            ThTaskMonitorGateway taskMonitorGateway) {
        this.taskService = taskService;
        this.preprocessService = preprocessService;
        this.audioProcessService = audioProcessService;
        this.objectMapper = objectMapper;
        this.mqFactory = mqFactory;
        this.cpuIntensiveExecutor = cpuIntensiveExecutor;
        this.redisConnectionFactory = redisConnectionFactory;
        this.taskMonitorGateway = taskMonitorGateway;
    }

    @Override
    public void run(String... args) {
        log.info("========== Task worker starting ==========");

        consumerThreads = new Thread[CONSUMER_THREAD_COUNT];
        for (int i = 0; i < CONSUMER_THREAD_COUNT; i++) {
            consumerThreads[i] = new Thread(this::consumeTasks, "Redis-Consumer-" + (i + 1));
            consumerThreads[i].setDaemon(true);
            consumerThreads[i].start();
        }

        log.info("Started {} consumer threads, read timeout={}s", CONSUMER_THREAD_COUNT, READ_TIMEOUT_SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        log.info("========== Task worker shutting down ==========");
        running.set(false);

        if (consumerThreads != null) {
            for (Thread thread : consumerThreads) {
                if (thread != null) {
                    thread.interrupt();
                }
            }
        }
    }

    private void consumeTasks() {
        String threadName = Thread.currentThread().getName();
        log.info("Consumer thread [{}] started", threadName);

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

                log.info("Consumer thread [{}] received task={}, deliveryId={}",
                        threadName, message.getTaskId(), message.getDeliveryId());
                cpuIntensiveExecutor.submit(() -> processTask(message));
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
                log.error("Consumer thread [{}] failed", threadName, e);
                sleepSafely(1000);
            }
        }

        log.info("Consumer thread [{}] stopped", threadName);
    }

    private void waitForRecovery(String threadName) {
        ThMessageQueue mq = mqFactory.getActiveMQ();
        int checkCount = 0;

        while (running.get() && !mq.isAvailable()) {
            checkCount++;
            if (checkCount % 12 == 1) {
                log.warn("Consumer thread [{}] waiting for Redis recovery, waited about {}s",
                        threadName, checkCount * RECOVERY_CHECK_INTERVAL_MS / 1000);
            }

            sleepSafely(RECOVERY_CHECK_INTERVAL_MS);

            if (mq instanceof ThRedisMessageQueue redisMQ && pingCheck()) {
                redisMQ.markAvailable();
                log.info("Consumer thread [{}] detected Redis recovery", threadName);
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

    private void processTask(ThMessage message) {
        String taskId = message.getTaskId();
        String deliveryId = message.getDeliveryId();
        String threadName = Thread.currentThread().getName();

        try {
            taskMonitorGateway.traceTaskProcessing(taskId, trace -> {
                log.info("Worker [{}] starts processing task={}, deliveryId={}", threadName, taskId, deliveryId);

                var task = taskService.getTask(taskId);
                if (task == null) {
                    log.warn("Task not found, skip: {}", taskId);
                    mqFactory.getActiveMQ().ackTask(deliveryId);
                    trace.markStatus("skipped_not_found");
                    return;
                }

                String currentStatus = task.getStatus();
                if ("completed".equals(currentStatus) || "failed".equals(currentStatus) || "cancelled".equals(currentStatus)) {
                    log.info("Task already terminal, skip duplicate consume: taskId={}, status={}", taskId, currentStatus);
                    mqFactory.getActiveMQ().ackTask(deliveryId);
                    trace.markStatus("skipped_" + currentStatus);
                    return;
                }

                taskService.startTask(taskId);
                trace.markStatus("processing");

                String requestData = taskService.getRequestData(taskId);
                if (requestData == null) {
                    taskService.failTask(taskId, "Request data does not exist");
                    mqFactory.getActiveMQ().ackTask(deliveryId);
                    trace.fail("Request data does not exist", null);
                    return;
                }

                ThFileUploadRequest request = objectMapper.readValue(requestData, ThFileUploadRequest.class);
                request.setTaskId(taskId);

                taskService.updateProgress(taskId, 10, "正在解析文档");
                var result = preprocessService.uploadAndProcessWithConfig(request);

                String podcastScript = result.getAiResult();
                if (podcastScript != null && !podcastScript.isEmpty()) {
                    taskService.updateProgress(taskId, 50, "正在生成播客音频");
                    try {
                        String audioUrl = audioProcessService.generatePodcastAudio(podcastScript, taskId);
                        log.info("Podcast audio generated: taskId={}, audioUrl={}", taskId, audioUrl);
                    } catch (Exception e) {
                        log.error("Audio generation failed but task continues with text result only: taskId={}", taskId, e);
                    }
                }

                taskService.updateProgress(taskId, 95, "正在生成结果");

                if (result.getOutputFileUrl() != null) {
                    task.setOutputFileName(result.getOutputFileName());
                    task.setOutputFileUrl(result.getOutputFileUrl());
                    if (result.getAiResult() != null) {
                        task.setOutputFileSize((long) result.getAiResult().getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
                    }
                    log.info("Task output file info: name={}, url={}", result.getOutputFileName(), result.getOutputFileUrl());
                }

                String resultJson = objectMapper.writeValueAsString(result);
                taskService.completeTask(taskId, resultJson);

                mqFactory.getActiveMQ().ackTask(deliveryId);
                trace.markStatus("completed");
                log.info("Task completed and acked: taskId={}, deliveryId={}", taskId, deliveryId);
            });
        } catch (Exception e) {
            log.error("Task processing failed: taskId={}, deliveryId={}", taskId, deliveryId, e);
            taskService.failTask(taskId, e.getMessage());
            mqFactory.getActiveMQ().ackTask(deliveryId);
        }
    }
}
