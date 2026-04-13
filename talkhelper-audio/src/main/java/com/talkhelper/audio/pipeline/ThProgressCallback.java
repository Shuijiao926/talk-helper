package com.talkhelper.audio.pipeline;

/**
 * 进度回调接口
 * 用于Pipeline各Handler上报处理进度
 */
@FunctionalInterface
public interface ThProgressCallback {

    /**
     * 上报进度
     *
     * @param taskId 任务ID
     * @param progress 进度百分比（0-100）
     * @param stage 当前阶段描述
     */
    void updateProgress(String taskId, int progress, String stage);
}
