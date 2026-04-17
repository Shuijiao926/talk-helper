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

@Slf4j
@Component
@RequiredArgsConstructor
public class ThTaskPersistenceHandler implements ThTaskCreateHandler {

    private final ThTaskMapper taskMapper;
    private final ThMessageQueueFactory mqFactory;

    @Override
    public void handle(ThTaskCreateContext context) {
        try {
            if (!context.isSuccess() && context.getErrorMessage() != null) {
                return;
            }

            ThTaskEntity task = ThTaskEntity.builder()
                    .taskId(context.getTaskId())
                    .userId(context.getUserId())
                    .taskType(context.getTaskType().name())
                    .status(ThTaskStatus.PENDING.getCode())
                    .progress(0)
                    .currentStage("任务已创建")
                    .requestDataUrl(context.getRequestDataUrl())
                    .inputFileName(context.getInputFileName())
                    .inputFileUrl(context.getInputFileUrl())
                    .inputFileSize(context.getInputFileSize())
                    .build();

            taskMapper.insert(task);
            log.debug("任务保存到数据库成功: taskId={}", context.getTaskId());

            mqFactory.getActiveMQ().sendTask(context.getTaskId());
            log.debug("任务加入消息队列成功: taskId={}", context.getTaskId());

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
