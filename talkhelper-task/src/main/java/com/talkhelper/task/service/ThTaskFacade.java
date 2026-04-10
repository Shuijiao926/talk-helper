package com.talkhelper.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 任务门面服务（Facade模式）
 * 封装任务创建的复杂逻辑，提供简洁接口给Controller
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThTaskFacade {

    private final ThAsyncTaskService taskService;
    private final ObjectMapper objectMapper;

    /**
     * 创建文本预处理任务（简单上传）
     *
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 任务信息（taskId + message）
     */
    public Map<String, String> createTextPreprocessTask(MultipartFile file, String userId) {
        try {
            // 1. 构建请求对象
            ThFileUploadRequest request = ThFileUploadRequest.builder()
                    .file(file)
                    .build();

            // 2. 序列化请求数据
            String requestData = serializeRequest(request);

            // 3. 创建异步任务
            String taskId = taskService.createTask(
                    resolveUserId(userId),
                    "text-preprocess",
                    requestData,
                    file.getOriginalFilename(),
                    file.getSize()
            );

            // 4. 返回任务信息
            return buildTaskResponse(taskId);

        } catch (Exception e) {
            log.error("创建文本预处理任务失败", e);
            throw new RuntimeException("任务创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建文本预处理任务（带配置）
     *
     * @param request 上传请求（包含配置）
     * @param userId 用户ID
     * @return 任务信息（taskId + message）
     */
    public Map<String, String> createTextPreprocessTaskWithConfig(ThFileUploadRequest request, String userId) {
        try {
            // 1. 提取文件信息（在序列化前）
            MultipartFile file = request.getFile();
            String fileName = file != null ? file.getOriginalFilename() : ThConstants.UNKNOWN;
            Long fileSize = file != null ? file.getSize() : 0L;

            // 2. 清除file字段（避免序列化问题）
            request.setFile(null);

            // 3. 序列化请求数据
            String requestData = serializeRequest(request);

            // 4. 创建异步任务
            String taskId = taskService.createTask(
                    resolveUserId(userId),
                    "text-preprocess",
                    requestData,
                    fileName,
                    fileSize
            );

            // 5. 返回任务信息
            return buildTaskResponse(taskId);

        } catch (Exception e) {
            log.error("创建文本预处理任务失败", e);
            throw new RuntimeException("任务创建失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务实体
     */
    public Object getTaskStatus(String taskId) {
        var task = taskService.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        return task;
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否成功
     */
    public boolean cancelTask(String taskId) {
        boolean success = taskService.cancelTask(taskId);
        if (!success) {
            throw new IllegalStateException("任务无法取消（可能已完成或不存在）");
        }
        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 序列化请求对象
     */
    private String serializeRequest(Object request) throws JsonProcessingException {
        return objectMapper.writeValueAsString(request);
    }

    /**
     * 解析用户ID（临时实现，后续从JWT获取）
     */
    private String resolveUserId(String userId) {
        return userId != null ? userId : "anonymous";
    }

    /**
     * 构建任务响应
     */
    private Map<String, String> buildTaskResponse(String taskId) {
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("message", "任务已创建，请通过WebSocket订阅进度");
        return response;
    }
}
