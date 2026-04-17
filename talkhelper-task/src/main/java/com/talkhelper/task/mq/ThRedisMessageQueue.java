package com.talkhelper.task.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis Streams 消息队列实现
 * 
 * 替代原有的 List(BLPOP) 方案，提供：
 * - 消费者组：多消费者协同，消息不重复消费
 * - ACK确认：任务处理完成后确认，防止消息丢失
 * - Pending消息恢复：自动重投递超时未确认的消息
 * - 消息持久化：Streams 天然持久化，重启不丢消息
 */
@Slf4j
@Component
public class ThRedisMessageQueue implements ThMessageQueue {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;
    private final StreamOperations<String, Object, Object> streamOps;

    private static final String STREAM_KEY = "task:stream";
    private static final String CONSUMER_GROUP = "task-workers";
    private static final long DEFAULT_TIMEOUT_SECONDS = 30;
    private static final long PENDING_MESSAGE_TIMEOUT_MINUTES = 5;

    private final AtomicBoolean available = new AtomicBoolean(true);
    private volatile String consumerName;

    public ThRedisMessageQueue(RedisTemplate<String, Object> redisTemplate,
                               RedisConnectionFactory connectionFactory) {
        this.redisTemplate = redisTemplate;
        this.connectionFactory = connectionFactory;
        this.streamOps = redisTemplate.opsForStream();
        this.consumerName = "consumer-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        ensureConsumerGroup();
    }

    private void ensureConsumerGroup() {
        try {
            streamOps.createGroup(STREAM_KEY, CONSUMER_GROUP);
            log.info("Redis Stream消费者组创建成功: stream={}, group={}", STREAM_KEY, CONSUMER_GROUP);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("消费者组已存在: stream={}, group={}", STREAM_KEY, CONSUMER_GROUP);
            } else {
                log.warn("创建消费者组异常（Stream可能尚未创建，将在首次发送时自动创建）: {}", e.getMessage());
            }
        }
    }

    @Override
    public String getType() {
        return "redis";
    }

    @Override
    public boolean isAvailable() {
        return available.get();
    }

    public void markAvailable() {
        if (available.compareAndSet(false, true)) {
            log.info("Redis连接已恢复，消息队列重新可用");
        }
    }

    public void markUnavailable() {
        if (available.compareAndSet(true, false)) {
            log.warn("Redis连接断开，消息队列不可用");
        }
    }

    @Override
    public void sendTask(String taskId) {
        if (!isAvailable()) {
            log.warn("Redis不可用，任务发送失败: {}", taskId);
            throw new IllegalStateException("Redis消息队列不可用");
        }

        try {
            Map<String, Object> body = Map.of("taskId", taskId);
            RecordId recordId = streamOps.add(STREAM_KEY, body);
            if (recordId == null) {
                throw new IllegalStateException("XADD返回null，任务发送失败: " + taskId);
            }
            log.debug("任务已发送到Redis Stream: taskId={}, entryId={}", taskId, recordId.getValue());
        } catch (Exception e) {
            markUnavailable();
            throw new IllegalStateException("任务发送失败: " + taskId, e);
        }
    }

    @Override
    public ThMessage receiveTask(long timeoutSeconds) {
        if (!isAvailable()) {
            return null;
        }

        try {
            long timeout = timeoutSeconds > 0 ? timeoutSeconds : DEFAULT_TIMEOUT_SECONDS;

            ThMessage message = readFromNewMessages(timeout);
            if (message != null) {
                return message;
            }

            return claimPendingMessage();
        } catch (Exception e) {
            log.error("从Redis Stream接收任务失败", e);
            markUnavailable();
            return null;
        }
    }

    private ThMessage readFromNewMessages(long timeoutSeconds) {
        StreamReadOptions options = StreamReadOptions.empty()
                .count(1)
                .block(Duration.ofSeconds(timeoutSeconds));

        List<MapRecord<String, Object, Object>> records = streamOps.read(
                Consumer.from(CONSUMER_GROUP, consumerName),
                options,
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
        );

        if (records == null || records.isEmpty()) {
            return null;
        }

        MapRecord<String, Object, Object> record = records.get(0);
        String taskId = extractTaskId(record);
        if (taskId != null) {
            return new ThMessage(taskId, record.getId().getValue());
        }
        ackTask(record.getId().getValue());
        return null;
    }

    private ThMessage claimPendingMessage() {
        try {
            // 直接查询当前消费者的pending消息
            var pendingInfo = streamOps.pending(STREAM_KEY, Consumer.from(CONSUMER_GROUP, consumerName),
                    org.springframework.data.domain.Range.unbounded(), 10L);

            if (pendingInfo == null || pendingInfo.isEmpty()) {
                return null;
            }

            for (PendingMessage pendingMsg : pendingInfo) {
                if (pendingMsg.getElapsedTimeSinceLastDelivery().toMinutes() >= PENDING_MESSAGE_TIMEOUT_MINUTES) {
                    String messageId = pendingMsg.getIdAsString();

                    List<MapRecord<String, Object, Object>> claimed = streamOps.claim(
                            STREAM_KEY,
                            CONSUMER_GROUP,
                            consumerName,
                            Duration.ofMinutes(PENDING_MESSAGE_TIMEOUT_MINUTES),
                            RecordId.of(messageId)
                    );

                    if (claimed != null && !claimed.isEmpty()) {
                        MapRecord<String, Object, Object> record = claimed.get(0);
                        String taskId = extractTaskId(record);
                        if (taskId != null) {
                            log.warn("重投递超时未确认消息: taskId={}, entryId={}, 原消费者={}",
                                    taskId, messageId, pendingMsg.getConsumerName());
                            return new ThMessage(taskId, record.getId().getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("检查Pending消息异常: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void ackTask(String deliveryId) {
        if (deliveryId == null) {
            return;
        }
        try {
            streamOps.acknowledge(STREAM_KEY, CONSUMER_GROUP, deliveryId);
            log.debug("消息已确认: entryId={}", deliveryId);
        } catch (Exception e) {
            log.error("消息确认失败: entryId={}", deliveryId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTaskId(MapRecord<String, Object, Object> record) {
        Object value = record.getValue().get("taskId");
        if (value == null) {
            log.warn("Stream消息缺少taskId字段: entryId={}", record.getId());
            return null;
        }
        return value.toString();
    }

    @Override
    public void shutdown() {
        available.set(false);
        log.info("Redis Stream消息队列关闭, consumer={}", consumerName);
    }
}
