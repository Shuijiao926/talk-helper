package com.talkhelper.web.controller;

import com.talkhelper.common.result.ThResult;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import com.talkhelper.web.annotation.ThApiLog;
import com.talkhelper.web.util.ThResultHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文本预处理控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/text-preprocess")
public class ThTextPreprocessController {

    private final ThTextPreprocessService textPreprocessService;

    /**
     * 上传文件并处理
     *
     * @param file 上传的文件
     * @return 处理结果
     */
    @ThApiLog("上传文件并处理")
    @PostMapping("/file/upload")
    public ThResult<ThPreprocessResultVO> uploadFile(@RequestParam("file") MultipartFile file) {
        return ThResultHelper.execute(
                () -> textPreprocessService.uploadAndProcess(file),
                "文件处理失败"
        );
    }

    /**
     * 上传文件并处理(带配置)
     *
     * @param request 上传请求
     * @return 处理结果
     */
    @ThApiLog("上传文件并处理(带配置)")
    @PostMapping("/upload-with-config")
    public ThResult<ThPreprocessResultVO> uploadFileWithConfig(ThFileUploadRequest request) {
        return ThResultHelper.execute(
                () -> textPreprocessService.uploadAndProcessWithConfig(request),
                "文件处理失败"
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
}
