package com.talkhelper.textpreprocess.service.impl;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.common.enums.ThFileType;
import com.talkhelper.common.util.ThSimpleMultipartFile;
import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessPipeline;
import com.talkhelper.textpreprocess.service.ThTextPreprocessService;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThTextPreprocessServiceImpl implements ThTextPreprocessService {

    private final ThTextProcessPipeline pipeline;
    private final ThObjectStorageGateway storageGateway;

    @Override
    public ThPreprocessResultVO uploadAndProcess(MultipartFile file) {
        return uploadAndProcessWithConfig(ThFileUploadRequest.builder().file(file).build());
    }

    @Override
    public ThPreprocessResultVO uploadAndProcessWithConfig(ThFileUploadRequest request) {
        try {
            MultipartFile file = request.getFile();
            String inputFileUrl = request.getInputFileUrl();

            ThFileType fileType;
            if (file != null) {
                fileType = detectFileType(file);
            } else if (inputFileUrl != null && !inputFileUrl.isEmpty()) {
                fileType = detectFileTypeFromUrl(inputFileUrl);
            } else {
                throw new IllegalArgumentException("文件或文件 URL 不能为空");
            }
            request.setFileType(fileType);

            if (file == null && inputFileUrl != null) {
                file = downloadFileFromUrl(inputFileUrl);
                request.setFile(file);
            }

            ThTextProcessContext context = ThTextProcessContext.builder()
                    .file(request.getFile())
                    .filePath(null)
                    .request(request)
                    .fileType(fileType.getCode())
                    .taskId(request.getTaskId())
                    .build();

            pipeline.execute(context);

            MultipartFile processedFile = request.getFile();
            log.info(ThLogConstants.FILE_PROCESS_COMPLETE,
                    processedFile != null ? processedFile.getOriginalFilename() : "unknown");
            return context.getResult();
        } catch (Exception e) {
            MultipartFile failedFile = request.getFile();
            log.error(ThLogConstants.FILE_PROCESS_FAILED,
                    failedFile != null ? failedFile.getOriginalFilename() : "unknown", e);
            return buildErrorResult(request.getFile(), e.getMessage());
        }
    }

    @Override
    public String[] getSupportedFileTypes() {
        return Arrays.stream(ThFileType.values())
                .map(ThFileType::getCode)
                .toArray(String[]::new);
    }

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
        return ThFileType.fromCode(extension);
    }

    private ThFileType detectFileTypeFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex == -1) {
                throw new IllegalArgumentException("无法从 URL 识别文件类型: " + fileUrl);
            }
            String extension = path.substring(dotIndex + 1).toLowerCase();
            return ThFileType.fromCode(extension);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的文件 URL: " + fileUrl, e);
        }
    }

    private MultipartFile downloadFileFromUrl(String fileUrl) {
        try {
            log.info("Downloading file from object storage: {}", fileUrl);

            byte[] data = storageGateway.downloadByUrl(fileUrl);
            String fileName = extractFileName(fileUrl);
            String contentType = getContentType(fileName);

            return new ThSimpleMultipartFile("file", fileName, contentType, data);
        } catch (Exception e) {
            log.error("Failed to download file from URL: {}", fileUrl, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    private String extractFileName(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        String path = url.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".md")) {
            return "text/markdown";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        }
        return "application/octet-stream";
    }

    private ThPreprocessResultVO buildErrorResult(MultipartFile file, String errorMessage) {
        String fileType = ThConstants.UNKNOWN;
        try {
            if (file != null) {
                ThFileType detectedType = detectFileType(file);
                fileType = detectedType.getCode();
            }
        } catch (Exception ignored) {
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
