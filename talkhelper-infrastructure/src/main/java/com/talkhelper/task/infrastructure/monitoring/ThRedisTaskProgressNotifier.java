package com.talkhelper.task.infrastructure.monitoring;

import com.talkhelper.common.util.ThRedisUtils;
import com.talkhelper.task.domain.monitoring.ThTaskProgressNotifier;
import com.talkhelper.task.dto.ThTaskProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThRedisTaskProgressNotifier implements ThTaskProgressNotifier {

    private final ThRedisUtils redisUtils;

    @Override
    public void notifyTaskProgress(String taskId, ThTaskProgressDTO progress) {
        redisUtils.publish("task:progress:" + taskId, progress);
    }
}
