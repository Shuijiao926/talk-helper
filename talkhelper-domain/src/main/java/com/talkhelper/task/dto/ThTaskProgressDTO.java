package com.talkhelper.task.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThTaskProgressDTO {

    private String taskId;

    private String status;

    private Integer progress;

    private String currentStage;

    private String message;

    private String resultDataUrl;

    private String errorMessage;
}
