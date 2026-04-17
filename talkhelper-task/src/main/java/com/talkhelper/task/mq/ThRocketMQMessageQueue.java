package com.talkhelper.task.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@ConditionalOnProperty(name = "talkhelper.mq.type", havingValue = "rocketmq")
public class ThRocketMQMessageQueue implements ThMessageQueue {

    private final RocketMQTemplate rocketMQTemplate;

    private static final String TAG = "task";
    private static final String TOPIC = "talkhelper-task";

    private final BlockingQueue<ThMessage> localBuffer = new LinkedBlockingQueue<>(1000);
    private final AtomicBoolean available = new AtomicBoolean(true);

    public ThRocketMQMessageQueue(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public String getType() {
        return "rocketmq";
    }

    @Override
    public boolean isAvailable() {
        return available.get();
    }

    @Override
    public void sendTask(String taskId) {
        if (!isAvailable()) {
            throw new IllegalStateException("RocketMQ消息队列不可用");
        }

        String destination = TOPIC + ":" + TAG;
        rocketMQTemplate.asyncSend(destination, taskId, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.debug("任务已发送到RocketMQ: taskId={}, msgId={}", taskId, sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("任务发送RocketMQ失败: taskId={}", taskId, e);
                localBuffer.offer(new ThMessage(taskId, "fallback-" + System.nanoTime()));
            }
        });
    }

    @Override
    public ThMessage receiveTask(long timeoutSeconds) {
        if (!isAvailable()) {
            return null;
        }

        try {
            return localBuffer.poll(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public void ackTask(String deliveryId) {
        log.debug("RocketMQ任务已确认: deliveryId={}", deliveryId);
    }

    public void deliverToLocalBuffer(String taskId) {
        ThMessage message = new ThMessage(taskId, "rocketmq-" + System.nanoTime());
        if (!localBuffer.offer(message)) {
            log.warn("本地缓冲区已满，任务暂存失败: taskId={}", taskId);
        }
    }

    @Override
    public void shutdown() {
        available.set(false);
        localBuffer.clear();
        log.info("RocketMQ消息队列关闭");
    }
}
