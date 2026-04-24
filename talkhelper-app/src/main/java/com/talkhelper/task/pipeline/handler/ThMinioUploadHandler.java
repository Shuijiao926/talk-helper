package com.talkhelper.task.pipeline.handler;

import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.common.util.ThIdGenerator;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThMinioUploadHandler implements ThTaskCreateHandler {

    private final ThObjectStorageGateway storageGateway;

    @Override
    public void handle(ThTaskCreateContext context) {
        MultipartFile file = context.getFile();

        if (file == null || file.isEmpty()) {
            log.debug("File is empty, skip storage upload");
            return;
        }

        if (context.getInputFileUrl() != null && !context.getInputFileUrl().isEmpty()) {
            log.debug("Input file url already exists, skip storage upload: {}", context.getInputFileUrl());
            return;
        }

        try {
            String taskId = ThIdGenerator.generateTaskId();
            String fileName = context.getInputFileName();
            String objectKey = "task/" + taskId + "/" + fileName;

            String fileUrl = storageGateway.uploadFile(objectKey, file);

            context.setTaskId(taskId);
            context.setInputFileUrl(fileUrl);

            log.info("Input file uploaded successfully: taskId={}, url={}", taskId, fileUrl);
        } catch (Exception e) {
            log.error("Failed to upload input file", e);
            context.setSuccess(false);
            context.setErrorMessage("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public int maxRetry() {
        return 2;
    }

    @Override
    public long retryDelayMs() {
        return 2000;
    }
}
