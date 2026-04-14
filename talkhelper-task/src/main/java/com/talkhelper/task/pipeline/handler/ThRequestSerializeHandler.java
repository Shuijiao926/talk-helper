package com.talkhelper.task.pipeline.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 请求数据序列化Handler
 * 将ThFileUploadRequest序列化为JSON,并清除file字段
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThRequestSerializeHandler implements ThTaskCreateHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(ThTaskCreateContext context) {
        // 从上下文中获取已提取的文件信息
        String fileName = context.getInputFileName();
        Long fileSize = context.getInputFileSize();

        // 构建请求对象并序列化
        try {
            MultipartFile file = context.getFile();
            
            ThFileUploadRequest request = ThFileUploadRequest.builder()
                    .file(file)
                    .build();

            // 清除file字段（避免序列化问题）
            request.setFile(null);

            // 序列化请求数据
            String requestData = objectMapper.writeValueAsString(request);
            context.setRequestData(requestData);

            log.debug("请求数据序列化完成: fileName={}, size={}", fileName, fileSize);

        } catch (JsonProcessingException e) {
            log.error("请求数据序列化失败", e);
            context.setSuccess(false);
            context.setErrorMessage("请求数据序列化失败: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
