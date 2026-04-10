package com.talkhelper.task.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 任务进度DTO（用于WebSocket推送）
 */
@Data
@Builder
public class ThTaskProgressDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 进度百分比（0-100）
     */
    private Integer progress;

    /**
     * 当前阶段描述
     */
    private String currentStage;

    /**
     * 消息
     */
    private String message;

    /**
     * 结果数据（仅在完成时返回）
     */
    private Object resultData;

    /**
     * 错误信息（仅在失败时返回）
     */
    private String errorMessage;
}
