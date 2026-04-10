package com.talkhelper.task.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.task.mq.ThMessageQueueFactory;
import com.talkhelper.task.service.ThAsyncTaskService;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * 任务Worker - 消费消息队列中的任务
 * 支持多种MQ实现：Redis、RabbitMQ、Kafka等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThTaskWorker implements CommandLineRunner {

    private final ThAsyncTaskService taskService;
    private final ThTextPreprocessService preprocessService;
    private final ObjectMapper objectMapper;
    private final ThMessageQueueFactory mqFactory; // 消息队列工厂
    private final ExecutorService cpuIntensiveExecutor; // CPU密集型线程池

    @Override
    public void run(String... args) {
        log.info("========== 任务Worker启动 ==========");
        
        // 使用CPU密集型线程池处理任务
        int workerCount = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < workerCount; i++) {
            cpuIntensiveExecutor.submit(this::consumeTasks);
        }
        
        log.info("已提交 {} 个Worker任务到CPU线程池", workerCount);
    }

    /**
     * 消费任务
     */
    private void consumeTasks() {
        log.info("Worker [{}] 开始消费任务", Thread.currentThread().getName());
        
        int consecutiveFailures = 0; // 连续失败次数
        final int MAX_CONSECUTIVE_FAILURES = 10; // 最大连续失败次数
        final long BACKOFF_BASE_MS = 1000; // 退避基数（1秒）
        
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 检查MQ是否可用
                if (!mqFactory.getActiveMQ().isAvailable()) {
                    log.warn("消息队列不可用，Worker进入等待状态");
                    consecutiveFailures++;
                    
                    // 指数退避：1s, 2s, 4s, 8s... 最多30s
                    long backoffTime = Math.min(
                        BACKOFF_BASE_MS * (1L << Math.min(consecutiveFailures, 5)),
                        30000
                    );
                    Thread.sleep(backoffTime);
                    continue;
                }
                
                // MQ可用，重置失败计数
                consecutiveFailures = 0;
                
                // 从队列中获取任务
                String taskId = taskService.pollTask();
                
                if (taskId == null) {
                    // 队列为空，短暂休眠
                    Thread.sleep(100);
                    continue;
                }

                log.info("Worker [{}] 开始处理任务: {}", Thread.currentThread().getName(), taskId);
                processTask(taskId);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Worker被中断");
                break;
            } catch (Exception e) {
                log.error(ThLogConstants.BUSINESS_EXCEPTION, e);
                
                // 发生异常，增加失败计数
                consecutiveFailures++;
                if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                    log.error("连续{}次失败，Worker停止", consecutiveFailures);
                    break;
                }
            }
        }
        
        log.info("Worker [{}] 已停止", Thread.currentThread().getName());
    }

    /**
     * 处理单个任务
     */
    private void processTask(String taskId) {
        try {
            // 1. 更新状态为处理中
            taskService.startTask(taskId);

            // 2. 获取任务信息
            var task = taskService.getTask(taskId);
            if (task == null) {
                taskService.failTask(taskId, "任务不存在");
                return;
            }

            // 3. 检查是否已取消
            if ("cancelled".equals(task.getStatus())) {
                log.info("任务已取消，跳过执行: {}", taskId);
                return;
            }

            // 4. 解析请求数据
            ThFileUploadRequest request = objectMapper.readValue(
                    task.getRequestData(), 
                    ThFileUploadRequest.class
            );

            // 5. 更新进度：开始解析
            taskService.updateProgress(taskId, 10, "正在解析文档");

            // 6. 执行文本预处理（同步）
            // TODO: 这里需要改造为支持进度回调
            var result = preprocessService.uploadAndProcessWithConfig(request);

            // 7. 更新进度：完成
            taskService.updateProgress(taskId, 90, "正在生成结果");

            // 8. 完成任务
            String resultJson = objectMapper.writeValueAsString(result);
            taskService.completeTask(taskId, resultJson);

            log.info("任务处理完成: {}", taskId);

        } catch (Exception e) {
            log.error("任务处理失败: {}", taskId, e);
            taskService.failTask(taskId, e.getMessage());
        }
    }
}
