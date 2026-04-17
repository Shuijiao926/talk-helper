package com.talkhelper.textpreprocess.pipeline;

public interface ThTextProcessHandler {

    String getName();

    void handle(ThTextProcessContext context) throws Exception;

    default boolean shouldHandle(ThTextProcessContext context) {
        return true;
    }

    default int maxRetry() {
        return 0;
    }

    default long retryDelayMs() {
        return 1000;
    }
}
