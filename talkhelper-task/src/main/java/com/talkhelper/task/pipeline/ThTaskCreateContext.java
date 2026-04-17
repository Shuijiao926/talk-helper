package com.talkhelper.task.pipeline;

import com.talkhelper.common.enums.ThTaskType;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ThTaskCreateContext {

    private MultipartFile file;

    private String userId;

    private ThTaskType taskType;

    private String requestData;

    private String requestDataUrl;

    private String inputFileName;

    private String inputFileUrl;

    private Long inputFileSize;

    private String taskId;

    private String errorMessage;

    @Builder.Default
    private boolean success = false;
}
