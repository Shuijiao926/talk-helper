package com.talkhelper.textpreprocess.service.impl;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.common.enums.ThFileType;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessPipeline;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 文本预处理服务实现
 * 使用管道模式处理文本，每个步骤由独立的处理器负责
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThTextPreprocessServiceImpl implements ThTextPreprocessService {

    private final ThTextProcessPipeline pipeline;

    @Override
    public ThPreprocessResultVO uploadAndProcess(MultipartFile file) {
        return uploadAndProcessWithConfig(ThFileUploadRequest.builder().file(file).build());
    }

    @Override
    public ThPreprocessResultVO uploadAndProcessWithConfig(ThFileUploadRequest request) {
        try {
            // 1. 检测文件类型
            ThFileType fileType = detectFileType(request.getFile());
            request.setFileType(fileType);

            // 2. 构建处理上下文
            ThTextProcessContext context = ThTextProcessContext.builder()
                    .file(request.getFile())
                    .request(request)
                    .fileType(fileType.getCode())
                    .build();

            // 3. 执行管道处理
            pipeline.execute(context);

            // 4. 返回结果
            log.info(ThLogConstants.FILE_PROCESS_COMPLETE, request.getFile().getOriginalFilename());
            return context.getResult();
        } catch (Exception e) {
            log.error(ThLogConstants.FILE_PROCESS_FAILED, request.getFile().getOriginalFilename(), e);
            return buildErrorResult(request.getFile(), e.getMessage());
        }
    }

    @Override
    public String[] getSupportedFileTypes() {
        return Arrays.stream(ThFileType.values())
                .map(ThFileType::getCode)
                .toArray(String[]::new);
    }

    /**
     * 检测文件类型
     */
    private ThFileType detectFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException("无法识别文件类型: " + filename);
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase();
        
        // 使用枚举验证并返回
        return ThFileType.fromCode(extension);
    }

    /**
     * 构建错误结果
     */
    private ThPreprocessResultVO buildErrorResult(MultipartFile file, String errorMessage) {
        String fileType = ThConstants.UNKNOWN;
        try {
            ThFileType detectedType = detectFileType(file);
            fileType = detectedType.getCode();
        } catch (Exception e) {
            // 忽略
        }

        String fileName = file != null ? file.getOriginalFilename() : ThConstants.UNKNOWN;
        
        return ThPreprocessResultVO.builder()
                .originalFileName(fileName)
                .fileType(fileType)
                .status(ThConstants.STATUS_FAILED)
                .errorMessage(errorMessage)
                .build();
    }
}
