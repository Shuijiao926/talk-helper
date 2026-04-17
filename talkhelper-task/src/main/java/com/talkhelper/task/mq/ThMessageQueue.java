package com.talkhelper.task.mq;

/**
 * 消息队列接口（策略模式）
 * 支持多种MQ实现：Redis Streams、RabbitMQ、Kafka、RocketMQ等
 */
public interface ThMessageQueue {

    /**
     * 获取MQ类型名称
     */
    String getType();

    /**
     * 检查MQ是否可用
     */
    boolean isAvailable();

    /**
     * 发送任务到队列
     *
     * @param taskId 任务ID
     */
    void sendTask(String taskId);

    /**
     * 从队列中消费任务（阻塞式）
     *
     * @param timeoutSeconds 超时时间（秒）
     * @return 消息包装对象，包含任务ID和消息投递ID（用于ACK），超时无任务返回null
     */
    ThMessage receiveTask(long timeoutSeconds);

    /**
     * 确认任务处理完成
     * 用于支持至少一次消费语义，防止消息丢失
     *
     * @param deliveryId 消息投递ID（Stream Entry ID 或等效标识）
     */
    default void ackTask(String deliveryId) {
    }

    /**
     * 关闭MQ连接
     */
    void shutdown();
}
