package com.talkhelper.task.domain.monitoring;

@FunctionalInterface
public interface ThTaskMonitoringOperation {

    void run(ThTaskTraceScope scope) throws Exception;
}
