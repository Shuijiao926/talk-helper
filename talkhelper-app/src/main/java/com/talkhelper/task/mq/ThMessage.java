package com.talkhelper.task.mq;

/**
 * 消息队列消息包装
 * 封装任务ID和消息投递ID，支持ACK确认机制
 */
public class ThMessage {

    private final String taskId;
    private final String deliveryId;

    public ThMessage(String taskId, String deliveryId) {
        this.taskId = taskId;
        this.deliveryId = deliveryId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    @Override
    public String toString() {
        return "ThMessage{taskId='" + taskId + "', deliveryId='" + deliveryId + "'}";
    }
}
