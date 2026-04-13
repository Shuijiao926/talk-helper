package com.talkhelper.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.task.pipeline.ThTaskCreateContext;
import com.talkhelper.task.pipeline.ThTaskCreatePipeline;
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

    private final ObjectMapper objectMapper;
    private final ThTaskCreatePipeline taskCreatePipeline;
    private final ThAsyncTaskService taskService;  // 用于查询和取消任务

    /**
     * 创建文本预处理任务（简单上传）
     *
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 任务信息（taskId + message）
     */
    public Map<String, String> createTextPreprocessTask(MultipartFile file, String userId) {
        try {
            // 1. 构建上下文
            ThTaskCreateContext context = ThTaskCreateContext.builder()
                    .file(file)
                    .userId(resolveUserId(userId))
                    .taskType("text-preprocess")
                    .build();

            // 2. 执行Pipeline (自动完成: 序列化、上传MinIO、创建任务)
            taskCreatePipeline.execute(context);

            // 3. 检查结果
            if (!context.isSuccess()) {
                throw new RuntimeException("任务创建失败: " + context.getErrorMessage());
            }

            // 4. 返回任务信息
            return buildTaskResponse(context.getTaskId());

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
            // 1. 提取文件
            MultipartFile file = request.getFile();

            // 2. 构建上下文
            ThTaskCreateContext context = ThTaskCreateContext.builder()
                    .file(file)
                    .userId(resolveUserId(userId))
                    .taskType("text-preprocess")
                    .build();

            // 3. 执行Pipeline (自动完成: 序列化、上传MinIO、创建任务)
            taskCreatePipeline.execute(context);

            // 4. 检查结果
            if (!context.isSuccess()) {
                throw new RuntimeException("任务创建失败: " + context.getErrorMessage());
            }

            // 5. 返回任务信息
            return buildTaskResponse(context.getTaskId());

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
