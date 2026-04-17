package com.talkhelper.task.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "talkhelper.mq.type", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "talkhelper-task",
        consumerGroup = "talkhelper-task-consumer-group",
        selectorExpression = "task"
)
@RequiredArgsConstructor
public class ThRocketMQConsumerListener implements RocketMQListener<String> {

    private final ThRocketMQMessageQueue rocketMQMessageQueue;

    @Override
    public void onMessage(String taskId) {
        log.debug("RocketMQ消费者收到任务: {}", taskId);
        rocketMQMessageQueue.deliverToLocalBuffer(taskId);
    }
}
