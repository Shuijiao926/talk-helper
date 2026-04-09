package com.talkhelper.textpreprocess.service.impl;

import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessPipeline;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
            String fileType = detectFileType(request.getFile());
            request.setFileType(fileType);

            // 2. 构建处理上下文
            ThTextProcessContext context = ThTextProcessContext.builder()
                    .file(request.getFile())
                    .request(request)
                    .fileType(fileType)
                    .build();

            // 3. 执行管道处理
            pipeline.execute(context);

            // 4. 返回结果
            log.info("文件处理完成: {}", request.getFile().getOriginalFilename());
            return context.getResult();

        } catch (Exception e) {
            log.error("文件处理失败: {}", request.getFile().getOriginalFilename(), e);
            return buildErrorResult(request.getFile(), e.getMessage());
        }
    }

    @Override
    public String[] getSupportedFileTypes() {
        // TODO: 从管道处理器中获取支持的文件类型
        return new String[]{"txt", "md", "html", "pdf", "docx", "epub"};
    }

    /**
     * 检测文件类型
     */
    private String detectFileType(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            throw new IllegalArgumentException("无法识别文件类型: " + filename);
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase();

        // 映射常见扩展名
        if ("txt".equals(extension)) {
            return "txt";
        } else if ("md".equals(extension) || "markdown".equals(extension)) {
            return "md";
        } else if ("html".equals(extension) || "htm".equals(extension)) {
            return "html";
        } else if ("pdf".equals(extension)) {
            return "pdf";
        } else if ("docx".equals(extension)) {
            return "docx";
        } else if ("epub".equals(extension)) {
            return "epub";
        } else {
            throw new UnsupportedOperationException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 构建错误结果
     */
    private ThPreprocessResultVO buildErrorResult(MultipartFile file, String errorMessage) {
        String fileType = "unknown";
        try {
            fileType = detectFileType(file);
        } catch (Exception e) {
            // 忽略
        }

        return ThPreprocessResultVO.builder()
                .originalFileName(file != null ? file.getOriginalFilename() : "unknown")
                .fileType(fileType)
                .status("failed")
                .errorMessage(errorMessage)
                .build();
    }
}
