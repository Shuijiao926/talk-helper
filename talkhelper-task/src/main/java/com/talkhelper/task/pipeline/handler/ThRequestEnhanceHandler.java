package com.talkhelper.task.pipeline.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 请求数据增强Handler
 * 在MinIO上传后,将inputFileUrl注入到已序列化的requestData中
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThRequestEnhanceHandler implements ThTaskCreateHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(ThTaskCreateContext context) {
        // 如果没有文件URL,跳过
        if (context.getInputFileUrl() == null || context.getInputFileUrl().isEmpty()) {
            log.debug("没有文件URL,跳过请求数据增强");
            return;
        }

        // 如果没有requestData,跳过
        if (context.getRequestData() == null || context.getRequestData().isEmpty()) {
            log.debug("没有requestData,跳过请求数据增强");
            return;
        }

//        try {
//            // 解析已序列化的JSON
////            JsonNode rootNode = objectMapper.readTree(context.getRequestData());
//            ObjectNode objectNode = (ObjectNode) rootNode;
//
//            // 注入inputFileUrl
//            objectNode.put("inputFileUrl", context.getInputFileUrl());
//
//            // 重新序列化
//            String enhancedData = objectMapper.writeValueAsString(objectNode);
//            context.setRequestData(enhancedData);
//
//            log.debug("请求数据增强完成: inputFileUrl={}", context.getInputFileUrl());
//
//        } catch (JsonProcessingException e) {
//            log.error("请求数据增强失败, requestData={}", context.getRequestData(), e);
//            throw new RuntimeException("请求数据增强失败: " + e.getMessage(), e);
//        }
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
