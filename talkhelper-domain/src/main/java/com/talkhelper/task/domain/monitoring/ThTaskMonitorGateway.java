package com.talkhelper.task.domain.monitoring;

public interface ThTaskMonitorGateway {

    void traceTaskProcessing(String taskId, ThTaskMonitoringOperation operation);
}
