package com.talkhelper.task.domain.monitoring;

public interface ThTaskTraceScope {

    void markStatus(String status);

    void addAttribute(String key, String value);

    void fail(String message, Throwable throwable);
}
