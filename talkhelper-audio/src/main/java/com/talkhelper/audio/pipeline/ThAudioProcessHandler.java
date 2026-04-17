package com.talkhelper.audio.pipeline;

public interface ThAudioProcessHandler {

    String getName();

    void handle(ThAudioProcessContext context) throws Exception;

    default boolean shouldHandle(ThAudioProcessContext context) {
        return true;
    }

    default int maxRetry() {
        return 0;
    }

    default long retryDelayMs() {
        return 1000;
    }
}
