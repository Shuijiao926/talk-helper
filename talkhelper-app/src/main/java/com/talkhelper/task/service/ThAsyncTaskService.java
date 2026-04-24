package com.talkhelper.task.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.talkhelper.common.cache.ThMultiLevelCache;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.common.enums.ThTaskStatus;
import com.talkhelper.task.domain.monitoring.ThTaskProgressNotifier;
import com.talkhelper.task.dto.ThTaskProgressDTO;
import com.talkhelper.task.entity.ThTaskEntity;
import com.talkhelper.task.mapper.ThTaskMapper;
import com.talkhelper.task.mq.ThMessage;
import com.talkhelper.task.mq.ThMessageQueueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThAsyncTaskService {

    private final ThTaskMapper taskMapper;
    private final ThMultiLevelCache cache;
    private final ThMessageQueueFactory mqFactory;
    private final ThObjectStorageGateway storageGateway;
    private final ThTaskProgressNotifier taskProgressNotifier;

    private static final long TASK_EXPIRE_DAYS = 7;

    public String createTask(String userId, String taskType, String requestData,
                             String inputFileName, String inputFileUrl, Long inputFileSize) {
        String taskId = java.util.UUID.randomUUID().toString().replace("-", "");

        String requestDataUrl = null;
        if (requestData != null && !requestData.isEmpty()) {
            String objectKey = "task-data/" + taskId + "/request.json";
            requestDataUrl = storageGateway.uploadBytes(
                    objectKey,
                    requestData.getBytes(StandardCharsets.UTF_8),
                    "application/json"
            );
        }

        ThTaskEntity task = ThTaskEntity.builder()
                .taskId(taskId)
                .userId(userId)
                .taskType(taskType)
                .status(ThTaskStatus.PENDING.getCode())
                .progress(0)
                .currentStage("任务已创建")
                .requestDataUrl(requestDataUrl)
                .inputFileName(inputFileName)
                .inputFileUrl(inputFileUrl)
                .inputFileSize(inputFileSize)
                .build();

        taskMapper.insert(task);

        log.debug("任务插入成功, 自动生成id={}, taskId={}", task.getId(), task.getTaskId());

        // 写入多级缓存（L1 Caffeine + L2 Redis）
        cache.put("task:" + taskId, task, TASK_EXPIRE_DAYS * 86400);

        mqFactory.getActiveMQ().sendTask(taskId);

        log.info("任务创建成功: taskId={}, userId={}, type={}, inputFile={}",
                taskId, userId, taskType, inputFileName);
        return taskId;
    }

    public String pollTask() {
        ThMessage message = mqFactory.getActiveMQ().receiveTask(1);
        if (message != null) {
            mqFactory.getActiveMQ().ackTask(message.getDeliveryId());
            return message.getTaskId();
        }
        return null;
    }

    public void startTask(String taskId) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setStatus(ThTaskStatus.PROCESSING.getCode());
        task.setProgress(5);
        task.setCurrentStage("开始处理");
        task.setStartTime(LocalDateTime.now());

        updateTask(task);
        pushProgress(taskId, task);
    }

    public void updateProgress(String taskId, int progress, String stage) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return;
        }

        task.setProgress(progress);
        task.setCurrentStage(stage);

        updateTask(task);
        pushProgress(taskId, task);
    }

    public void completeTask(String taskId, String resultData) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return;
        }

        String resultDataUrl = null;
        if (resultData != null && !resultData.isEmpty()) {
            String objectKey = "task-data/" + taskId + "/result.json";
            resultDataUrl = storageGateway.uploadBytes(
                    objectKey,
                    resultData.getBytes(StandardCharsets.UTF_8),
                    "application/json"
            );
        }

        task.setStatus(ThTaskStatus.COMPLETED.getCode());
        task.setProgress(100);
        task.setCurrentStage("处理完成");
        task.setResultDataUrl(resultDataUrl);
        task.setEndTime(LocalDateTime.now());

        updateTask(task);
        pushProgress(taskId, task);

        log.info("任务完成: taskId={}", taskId);
    }

    public void failTask(String taskId, String errorMessage) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return;
        }

        task.setStatus(ThTaskStatus.FAILED.getCode());
        task.setErrorMessage(errorMessage);
        task.setEndTime(LocalDateTime.now());

        updateTask(task);
        pushProgress(taskId, task);

        log.error("任务失败: taskId={}, error={}", taskId, errorMessage);
    }

    public boolean cancelTask(String taskId) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return false;
        }

        if (!ThTaskStatus.PENDING.getCode().equals(task.getStatus())
                && !ThTaskStatus.PROCESSING.getCode().equals(task.getStatus())) {
            log.warn("任务状态不允许取消: taskId={}, status={}", taskId, task.getStatus());
            return false;
        }

        task.setStatus(ThTaskStatus.CANCELLED.getCode());
        task.setCurrentStage("已取消");
        task.setEndTime(LocalDateTime.now());

        updateTask(task);
        pushProgress(taskId, task);

        log.info("任务已取消: taskId={}", taskId);
        return true;
    }

    public ThTaskEntity getTask(String taskId) {
        String cacheKey = "task:" + taskId;

        return cache.get(cacheKey, () -> {
            log.debug("从数据库查询任务: {}", taskId);
            return taskMapper.selectOne(Wrappers.<ThTaskEntity>lambdaQuery()
                    .eq(ThTaskEntity::getTaskId, taskId));
        }, TASK_EXPIRE_DAYS * 86400);
    }

    public String getRequestData(String taskId) {
        ThTaskEntity task = getTask(taskId);
        if (task == null || task.getRequestDataUrl() == null) {
            return null;
        }
        byte[] data = storageGateway.download("task-data/" + taskId + "/request.json");
        return new String(data, StandardCharsets.UTF_8);
    }

    public String getResultData(String taskId) {
        ThTaskEntity task = getTask(taskId);
        if (task == null || task.getResultDataUrl() == null) {
            return null;
        }
        byte[] data = storageGateway.download("task-data/" + taskId + "/result.json");
        return new String(data, StandardCharsets.UTF_8);
    }

    private void updateTask(ThTaskEntity task) {
        taskMapper.updateById(task);

        // 更新多级缓存（DB已更新，同步写入L1+L2缓存）
        cache.put("task:" + task.getTaskId(), task, TASK_EXPIRE_DAYS * 86400);
    }

    private void pushProgress(String taskId, ThTaskEntity task) {
        ThTaskProgressDTO progressDTO = ThTaskProgressDTO.builder()
                .taskId(taskId)
                .status(task.getStatus())
                .progress(task.getProgress())
                .currentStage(task.getCurrentStage())
                .resultDataUrl(task.getResultDataUrl())
                .errorMessage(task.getErrorMessage())
                .build();

        taskProgressNotifier.notifyTaskProgress(taskId, progressDTO);
    }
}
