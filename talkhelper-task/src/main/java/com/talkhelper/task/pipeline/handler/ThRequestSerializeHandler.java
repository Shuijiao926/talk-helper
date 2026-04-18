package com.talkhelper.task.pipeline.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.storage.ThObjectStorageFactory;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThRequestSerializeHandler implements ThTaskCreateHandler {

    private final ObjectMapper objectMapper;
    private final ThObjectStorageFactory storageFactory;

    @Override
    public void handle(ThTaskCreateContext context) {
        String fileName = context.getInputFileName();
        Long fileSize = context.getInputFileSize();

        try {
            MultipartFile file = context.getFile();

            ThFileUploadRequest request = ThFileUploadRequest.builder()
                    .file(file)
                    .build();

            // file不可序列化，用MinIO URL替代（由ThMinioUploadHandler在order 2设置）
            request.setFile(null);
            request.setInputFileUrl(context.getInputFileUrl());

            String requestData = objectMapper.writeValueAsString(request);
            context.setRequestData(requestData);

            String objectKey = "task-data/" + context.getTaskId() + "/request.json";
            String url = storageFactory.getActiveStorage().uploadBytes(
                    requestData.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "talkhelper",
                    objectKey,
                    "application/json"
            );
            context.setRequestDataUrl(url);

            log.debug("请求数据序列化并上传完成: fileName={}, url={}", fileName, url);

        } catch (JsonProcessingException e) {
            log.error("请求数据序列化失败", e);
            context.setSuccess(false);
            context.setErrorMessage("请求数据序列化失败: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
