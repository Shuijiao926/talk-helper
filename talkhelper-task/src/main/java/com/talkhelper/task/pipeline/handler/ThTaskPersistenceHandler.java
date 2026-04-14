package com.talkhelper.task.pipeline.handler;

import com.talkhelper.common.enums.ThTaskStatus;
import com.talkhelper.task.entity.ThTaskEntity;
import com.talkhelper.task.mapper.ThTaskMapper;
import com.talkhelper.task.mq.ThMessageQueueFactory;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 任务持久化Handler
 * 将任务信息保存到数据库和Redis,并加入消息队列
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThTaskPersistenceHandler implements ThTaskCreateHandler {

    private final ThTaskMapper taskMapper;
    private final ThMessageQueueFactory mqFactory;
    // TODO: 注入RedisUtils用于缓存

    @Override
    public void handle(ThTaskCreateContext context) {
        try {
            // 如果之前步骤失败,直接返回
            if (!context.isSuccess() && context.getErrorMessage() != null) {
                return;
            }

            // 构建任务实体
            ThTaskEntity task = ThTaskEntity.builder()
                    .taskId(context.getTaskId())
                    .userId(context.getUserId())
                    .taskType(context.getTaskType().name())
                    .status(ThTaskStatus.PENDING.getCode())
                    .progress(0)
                    .currentStage("任务已创建")
                    .requestData(context.getRequestData())
                    .inputFileName(context.getInputFileName())
                    .inputFileUrl(context.getInputFileUrl())
                    .inputFileSize(context.getInputFileSize())
                    .build();

            // 保存到数据库
            taskMapper.insert(task);
            log.debug("任务保存到数据库成功: taskId={}", context.getTaskId());

            // TODO: 存入Redis缓存
            // redisUtils.set("task:" + context.getTaskId(), task, 7, TimeUnit.DAYS);

            // 加入消息队列
            mqFactory.getActiveMQ().sendTask(context.getTaskId());
            log.debug("任务加入消息队列成功: taskId={}", context.getTaskId());

            // 标记成功
            context.setSuccess(true);

            log.info("任务创建完成: taskId={}, userId={}, type={}", 
                    context.getTaskId(), context.getUserId(), context.getTaskType());

        } catch (Exception e) {
            log.error("任务创建失败", e);
            context.setSuccess(false);
            context.setErrorMessage("任务创建失败: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
