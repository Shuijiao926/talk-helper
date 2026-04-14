package com.talkhelper.web.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.talkhelper.common.annotation.ThApiLog;
import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.common.result.ThResult;
import com.talkhelper.common.util.ThResultHelper;
import com.talkhelper.task.service.ThTaskFacade;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 文本预处理控制器（异步模式）
 * 职责：仅负责HTTP层参数校验和响应，业务逻辑委托给Facade
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/text-preprocess")
public class ThTextPreprocessController {

    private final ThTaskFacade taskFacade;
    private final ThTextPreprocessService textPreprocessService;

    /**
     * 上传文件并处理
     *
     * @param file 上传的文件
     * @return 任务ID
     */
    @ThApiLog("上传文件并处理")
    @PostMapping("/file/upload")
    public ThResult<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        return ThResultHelper.execute(
                () -> taskFacade.createTextPreprocessTask(file, null)
        );
    }

    /**
     * 上传文件并处理(带配置，异步)
     *
     * @param request 上传请求
     * @return 任务ID
     */
    @ThApiLog("上传文件并处理(带配置)")
    @PostMapping("/upload-with-config")
    public ThResult<Map<String, String>> uploadFileWithConfig(ThFileUploadRequest request) {
        return ThResultHelper.execute(
                () -> taskFacade.createTextPreprocessTaskWithConfig(request, null)
        );
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    @SentinelResource(value = "queryTask", blockHandler = "handleBlock")
    @ThApiLog(value = "查询任务状态", logParams = false)
    @GetMapping("/task/{taskId}")
    public ThResult<?> getTaskStatus(@PathVariable String taskId) {
        return ThResultHelper.execute(
                () -> taskFacade.getTaskStatus(taskId)
        );
    }

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 取消结果
     */
    @ThApiLog("取消任务")
    @PostMapping("/task/{taskId}/cancel")
    public ThResult<Boolean> cancelTask(@PathVariable String taskId) {
        return ThResultHelper.execute(
                () -> taskFacade.cancelTask(taskId)
        );
    }

    /**
     * 获取支持的文件类型
     *
     * @return 支持的文件类型列表
     */
    @ThApiLog(value = "获取支持的文件类型", logParams = false)
    @GetMapping("/supported-types")
    public ThResult<String[]> getSupportedFileTypes() {
        return ThResultHelper.execute(
                () -> textPreprocessService.getSupportedFileTypes()
        );
    }

    /**
     * 限流降级处理
     */
    public ThResult<?> handleBlock(Exception e) {
        log.warn("请求被限流: {}", e.getMessage());
        return ThResult.error("系统繁忙，请稍后重试");
    }
}
