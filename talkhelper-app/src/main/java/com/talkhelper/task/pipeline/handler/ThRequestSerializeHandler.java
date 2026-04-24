package com.talkhelper.task.pipeline.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThRequestSerializeHandler implements ThTaskCreateHandler {

    private final ObjectMapper objectMapper;
    private final ThObjectStorageGateway storageGateway;

    @Override
    public void handle(ThTaskCreateContext context) {
        try {
            MultipartFile file = context.getFile();

            ThFileUploadRequest request = ThFileUploadRequest.builder()
                    .file(file)
                    .build();

            request.setFile(null);
            request.setInputFileUrl(context.getInputFileUrl());

            String requestData = objectMapper.writeValueAsString(request);
            context.setRequestData(requestData);

            String objectKey = "task-data/" + context.getTaskId() + "/request.json";
            String url = storageGateway.uploadBytes(
                    objectKey,
                    requestData.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    "application/json"
            );
            context.setRequestDataUrl(url);

            log.debug("Request data serialized and uploaded: fileName={}, url={}",
                    context.getInputFileName(), url);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize request data", e);
            context.setSuccess(false);
            context.setErrorMessage("请求数据序列化失败: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
