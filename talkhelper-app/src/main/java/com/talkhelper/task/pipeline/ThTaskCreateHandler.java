package com.talkhelper.task.pipeline;

public interface ThTaskCreateHandler {

    void handle(ThTaskCreateContext context);

    default int getOrder() {
        return 0;
    }

    default int maxRetry() {
        return 0;
    }

    default long retryDelayMs() {
        return 1000;
    }
}
