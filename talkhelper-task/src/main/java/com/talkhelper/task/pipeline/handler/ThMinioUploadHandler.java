package com.talkhelper.task.pipeline.handler;

import com.talkhelper.common.config.ThStorageConfig;
import com.talkhelper.common.storage.ThObjectStorageFactory;
import com.talkhelper.common.storage.ThObjectStorageStrategy;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * MinIO文件上传Handler
 * 将文件上传到MinIO,获取访问URL
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class ThMinioUploadHandler implements ThTaskCreateHandler {

    private final ThObjectStorageFactory storageFactory;
    private final ThStorageConfig storageConfig;

    @Override
    public void handle(ThTaskCreateContext context) {
        MultipartFile file = context.getFile();
        
        // 如果文件为空或已经有URL,跳过上传
        if (file == null || file.isEmpty()) {
            log.debug("文件为空,跳过MinIO上传");
            return;
        }

        if (context.getInputFileUrl() != null && !context.getInputFileUrl().isEmpty()) {
            log.debug("文件已有URL,跳过MinIO上传: {}", context.getInputFileUrl());
            return;
        }

        try {
            // 获取OSS策略
            ThObjectStorageStrategy storage = storageFactory.getActiveStorage();

            // 生成对象键: task/{taskId}/{fileName}
            String taskId = generateTaskId();
            String fileName = context.getInputFileName();
            String objectKey = "task/" + taskId + "/" + fileName;
            String bucketName = storageConfig.getDefaultBucket();

            // 上传文件
            String fileUrl = storage.uploadFile(file, bucketName, objectKey);

            // 更新上下文
            context.setTaskId(taskId);
            context.setInputFileUrl(fileUrl);

            log.info("文件上传MinIO成功: taskId={}, url={}", taskId, fileUrl);

        } catch (Exception e) {
            log.error("文件上传MinIO失败", e);
            context.setSuccess(false);
            context.setErrorMessage("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 生成任务ID
     */
    private String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
