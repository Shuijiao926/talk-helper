package com.talkhelper.task.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis消息队列实现（默认）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThRedisMessageQueue implements ThMessageQueue {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    
    private static final String TASK_QUEUE_KEY = "task:queue";
    private volatile boolean available = false;

    @Override
    public String getType() {
        return "redis";
    }

    @Override
    public boolean isAvailable() {
        if (available) {
            return true;
        }
        
        try {
            connectionFactory.getConnection().ping();
            available = true;
            return true;
        } catch (Exception e) {
            available = false;
            log.debug("Redis不可用: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void sendTask(String taskId) {
        if (!isAvailable()) {
            log.warn("Redis不可用，任务发送失败: {}", taskId);
            throw new IllegalStateException("Redis消息队列不可用");
        }
        redisTemplate.opsForList().rightPush(TASK_QUEUE_KEY, taskId);
        log.debug("任务已发送到Redis队列: {}", taskId);
    }

    @Override
    public String receiveTask(long timeoutSeconds) {
        if (!isAvailable()) {
            return null; // Redis不可用时返回null，避免异常
        }
        
        try {
            Object taskId = redisTemplate.opsForList().leftPop(TASK_QUEUE_KEY, timeoutSeconds, TimeUnit.SECONDS);
            return taskId != null ? taskId.toString() : null;
        } catch (Exception e) {
            log.error("从Redis队列接收任务失败", e);
            available = false;
            return null;
        }
    }

    @Override
    public void shutdown() {
        available = false;
        log.info("Redis消息队列关闭");
    }
}
