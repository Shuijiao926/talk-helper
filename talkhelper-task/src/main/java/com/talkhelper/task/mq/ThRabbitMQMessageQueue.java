package com.talkhelper.task.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ消息队列实现（示例，需添加依赖后启用）
 * 
 * 启用方式：
 * 1. 添加依赖：spring-boot-starter-amqp
 * 2. 配置：talkhelper.mq.type=rabbitmq
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "talkhelper.mq.type", havingValue = "rabbitmq")
public class ThRabbitMQMessageQueue implements ThMessageQueue {

    // private final RabbitTemplate rabbitTemplate;
    // private static final String QUEUE_NAME = "task.queue";

    // public ThRabbitMQMessageQueue(RabbitTemplate rabbitTemplate) {
    //     this.rabbitTemplate = rabbitTemplate;
    // }

    @Override
    public String getType() {
        return "rabbitmq";
    }

    @Override
    public boolean isAvailable() {
        // TODO: 实现RabbitMQ健康检查
        return false;
    }

    @Override
    public void sendTask(String taskId) {
        // rabbitTemplate.convertAndSend(QUEUE_NAME, taskId);
        log.debug("任务已发送到RabbitMQ: {}", taskId);
        throw new UnsupportedOperationException("RabbitMQ实现待完成");
    }

    @Override
    public String receiveTask(long timeoutSeconds) {
        // Object taskId = rabbitTemplate.receiveAndConvert(QUEUE_NAME, timeoutSeconds * 1000);
        // return taskId != null ? taskId.toString() : null;
        log.debug("从RabbitMQ接收任务");
        throw new UnsupportedOperationException("RabbitMQ实现待完成");
    }

    @Override
    public void shutdown() {
        log.info("RabbitMQ消息队列关闭");
    }
}
