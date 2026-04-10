package com.talkhelper.task.mq;

/**
 * 消息队列接口（策略模式）
 * 支持多种MQ实现：Redis、RabbitMQ、Kafka、RocketMQ等
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
     * @return 任务ID，如果超时无任务则返回null
     */
    String receiveTask(long timeoutSeconds);

    /**
     * 关闭MQ连接
     */
    void shutdown();
}
