package com.talkhelper.task.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.cache.ThMultiLevelCache;
import com.talkhelper.common.enums.ThTaskStatus;
import com.talkhelper.common.util.ThRedisUtils;
import com.talkhelper.task.dto.ThTaskProgressDTO;
import com.talkhelper.task.entity.ThTaskEntity;
import com.talkhelper.task.mapper.ThTaskMapper;
import com.talkhelper.task.mq.ThMessageQueueFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 异步任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThAsyncTaskService {

    private final ThTaskMapper taskMapper;
    private final ThMultiLevelCache cache;
    private final ThRedisUtils redisUtils;
    private final ThMessageQueueFactory mqFactory;

    // Redis Key前缀
    private static final String TASK_KEY_PREFIX = "task:";
    private static final long TASK_EXPIRE_DAYS = 7; // 任务保留7天

    /**
     * 创建任务
     * @param userId 用户ID
     * @param taskType 任务类型
     * @param requestData 请求数据JSON
     * @param inputFileName 输入文件名
     * @param inputFileUrl 输入文件MinIO URL
     * @param inputFileSize 输入文件大小
     */
    public String createTask(String userId, String taskType, String requestData,
                             String inputFileName, String inputFileUrl, Long inputFileSize) {
        // 生成任务ID(UUID)
        String taskId = java.util.UUID.randomUUID().toString().replace("-", "");
        
        ThTaskEntity task = ThTaskEntity.builder()
                .taskId(taskId)
                .userId(userId)
                .taskType(taskType)
                .status(ThTaskStatus.PENDING.getCode())
                .progress(0)
                .currentStage("任务已创建")
                .requestData(requestData)
                .inputFileName(inputFileName)
                .inputFileUrl(inputFileUrl)
                .inputFileSize(inputFileSize)
                .build();

        // 保存到数据库（id自增、createTime和updateTime由MyBatis自动填充）
        taskMapper.insert(task);
        
        log.debug("任务插入成功, 自动生成id={}, taskId={}", task.getId(), task.getTaskId());

        // 存入Redis（快速查询）
        String redisKey = TASK_KEY_PREFIX + taskId;
        redisUtils.set(redisKey, task, TASK_EXPIRE_DAYS, TimeUnit.DAYS);

        // 加入消息队列（通过工厂获取当前激活的MQ）
        mqFactory.getActiveMQ().sendTask(taskId);

        log.info("任务创建成功: taskId={}, userId={}, type={}, inputFile={}", 
                taskId, userId, taskType, inputFileName);
        return taskId;
    }

    /**
     * 从队列中获取下一个任务
     */
    public String pollTask() {
        return mqFactory.getActiveMQ().receiveTask(1);
    }

    /**
     * 更新任务状态为处理中
     */
    public void startTask(String taskId) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        task.setStatus(ThTaskStatus.PROCESSING.getCode());
        task.setProgress(5);
        task.setCurrentStage("开始处理");
        task.setStartTime(LocalDateTime.now());
        // updateTime由MyBatis自动填充

        updateTask(task);
        pushProgress(taskId, task);
    }

    /**
     * 更新任务进度
     */
    public void updateProgress(String taskId, int progress, String stage) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return;
        }

        task.setProgress(progress);
        task.setCurrentStage(stage);
        // updateTime由MyBatis自动填充

        updateTask(task);
        pushProgress(taskId, task);
    }

    /**
     * 完成任务
     */
    public void completeTask(String taskId, String resultData) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return;
        }

        task.setStatus(ThTaskStatus.COMPLETED.getCode());
        task.setProgress(100);
        task.setCurrentStage("处理完成");
        task.setResultData(resultData);
        task.setEndTime(LocalDateTime.now());
        // updateTime由MyBatis自动填充

        updateTask(task);
        pushProgress(taskId, task);

        log.info("任务完成: taskId={}", taskId);
    }

    /**
     * 任务失败
     */
    public void failTask(String taskId, String errorMessage) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return;
        }

        task.setStatus(ThTaskStatus.FAILED.getCode());
        task.setErrorMessage(errorMessage);
        task.setEndTime(LocalDateTime.now());
        // updateTime由MyBatis自动填充

        updateTask(task);
        pushProgress(taskId, task);

        log.error("任务失败: taskId={}, error={}", taskId, errorMessage);
    }

    /**
     * 取消任务
     */
    public boolean cancelTask(String taskId) {
        ThTaskEntity task = getTask(taskId);
        if (task == null) {
            return false;
        }

        // 只有待处理或处理中的任务可以取消
        if (!ThTaskStatus.PENDING.getCode().equals(task.getStatus()) 
                && !ThTaskStatus.PROCESSING.getCode().equals(task.getStatus())) {
            log.warn("任务状态不允许取消: taskId={}, status={}", taskId, task.getStatus());
            return false;
        }

        task.setStatus(ThTaskStatus.CANCELLED.getCode());
        task.setCurrentStage("已取消");
        task.setEndTime(LocalDateTime.now());
        // updateTime由MyBatis自动填充

        updateTask(task);
        pushProgress(taskId, task);

        log.info("任务已取消: taskId={}", taskId);
        return true;
    }

    /**
     * 获取任务信息（带缓存）
     * 注意：使用taskId(UUID)查询，而非id(自增主键)
     */
    public ThTaskEntity getTask(String taskId) {
        String cacheKey = "task:" + taskId;
        
        // 使用多级缓存，未命中时从数据库查询
        return cache.get(cacheKey, () -> {
            log.debug("从数据库查询任务: {}", taskId);
            return taskMapper.selectOne(Wrappers.<ThTaskEntity>lambdaQuery()
                    .eq(ThTaskEntity::getTaskId, taskId));
        }, TASK_EXPIRE_DAYS * 86400); // TTL转换为秒
    }

    /**
     * 更新任务（数据库+Redis）
     */
    private void updateTask(ThTaskEntity task) {
        taskMapper.updateById(task);
        
        String redisKey = TASK_KEY_PREFIX + task.getId();
        redisUtils.set(redisKey, task, TASK_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 推送进度到WebSocket（通过Redis Pub/Sub）
     */
    private void pushProgress(String taskId, ThTaskEntity task) {
        ThTaskProgressDTO progressDTO = ThTaskProgressDTO.builder()
                .taskId(taskId)
                .status(task.getStatus())
                .progress(task.getProgress())
                .currentStage(task.getCurrentStage())
                .resultData(task.getResultData())
                .errorMessage(task.getErrorMessage())
                .build();

        // 发布到Redis频道，WebSocket服务订阅
        redisUtils.publish("task:progress:" + taskId, progressDTO);
    }
}
