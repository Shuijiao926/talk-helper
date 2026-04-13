package com.talkhelper.textpreprocess.service.impl;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.constant.ThLogConstants;
import com.talkhelper.common.enums.ThFileType;
import com.talkhelper.common.storage.ThObjectStorageFactory;
import com.talkhelper.common.storage.ThObjectStorageStrategy;
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

/**
 * 文本预处理服务实现
 * 使用管道模式处理文本，每个步骤由独立的处理器负责
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThTextPreprocessServiceImpl implements ThTextPreprocessService {

    private final ThTextProcessPipeline pipeline;
    private final ThObjectStorageFactory storageFactory;

    @Override
    public ThPreprocessResultVO uploadAndProcess(MultipartFile file) {
        return uploadAndProcessWithConfig(ThFileUploadRequest.builder().file(file).build());
    }

    @Override
    public ThPreprocessResultVO uploadAndProcessWithConfig(ThFileUploadRequest request) {
        try {
            MultipartFile file = request.getFile();
            String inputFileUrl = request.getInputFileUrl();
            
            // 1. 检测文件类型
            ThFileType fileType;
            if (file != null) {
                fileType = detectFileType(file);
            } else if (inputFileUrl != null && !inputFileUrl.isEmpty()) {
                // 从URL推断文件类型
                fileType = detectFileTypeFromUrl(inputFileUrl);
            } else {
                throw new IllegalArgumentException("文件或文件URL不能为空");
            }
            request.setFileType(fileType);

            // 2. 如果文件来自MinIO URL，下载并转换为MultipartFile
            if (file == null && inputFileUrl != null) {
                file = downloadFileFromUrl(inputFileUrl);
                request.setFile(file);
            }

            // 3. 构建处理上下文（不再需要本地文件路径）
            ThTextProcessContext context = ThTextProcessContext.builder()
                    .file(request.getFile())
                    .filePath(null)  // 大文件已存MinIO，不需要本地路径
                    .request(request)
                    .fileType(fileType.getCode())
                    .build();

            // 4. 执行管道处理
            pipeline.execute(context);

            // 5. 返回结果
            log.info(ThLogConstants.FILE_PROCESS_COMPLETE, request.getFile().getOriginalFilename());
            return context.getResult();
        } catch (Exception e) {
            log.error(ThLogConstants.FILE_PROCESS_FAILED, 
                    request.getFile() != null ? request.getFile().getOriginalFilename() : "unknown", e);
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
     * 从URL推断文件类型
     */
    private ThFileType detectFileTypeFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex == -1) {
                throw new IllegalArgumentException("无法从URL识别文件类型: " + fileUrl);
            }
            String extension = path.substring(dotIndex + 1).toLowerCase();
            return ThFileType.fromCode(extension);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的文件URL: " + fileUrl, e);
        }
    }

    /**
     * 从MinIO URL下载文件并转换为MultipartFile
     */
    private MultipartFile downloadFileFromUrl(String fileUrl) {
        try {
            log.info("从 MinIO 下载文件: {}", fileUrl);
            
            // 获取OSS策略
            ThObjectStorageStrategy storage = storageFactory.getActiveStorage();
            
            // 从URL解析bucket和objectKey
            String[] parts = parseUrlToBucketAndKey(fileUrl);
            String bucketName = parts[0];
            String objectKey = parts[1];
            
            // 下载文件
            byte[] data = storage.downloadFile(bucketName, objectKey);
            
            // 转换为MultipartFile
            String fileName = objectKey.substring(objectKey.lastIndexOf('/') + 1);
            String contentType = getContentType(fileName);
            
            return new ThSimpleMultipartFile("file", fileName, contentType, data);
            
        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", fileUrl, e);
            throw new RuntimeException("文件下载失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析URL为bucket和objectKey
     */
    private String[] parseUrlToBucketAndKey(String fileUrl) {
        // URL格式: http://localhost:9000/bucket/path/to/file.pdf
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath(); // /bucket/path/to/file.pdf
            
            // 移除开头的/
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            
            // 第一个/之前是bucket，之后是objectKey
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) {
                throw new IllegalArgumentException("无效的MinIO URL格式: " + fileUrl);
            }
            
            String bucket = path.substring(0, firstSlash);
            String key = path.substring(firstSlash + 1);
            
            return new String[]{bucket, key};
        } catch (Exception e) {
            throw new IllegalArgumentException("解析MinIO URL失败: " + fileUrl, e);
        }
    }

    /**
     * 根据文件名获取Content-Type
     */
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

    /**
     * 构建错误结果
     */
    private ThPreprocessResultVO buildErrorResult(MultipartFile file, String errorMessage) {
        String fileType = ThConstants.UNKNOWN;
        try {
            if (file != null) {
                ThFileType detectedType = detectFileType(file);
                fileType = detectedType.getCode();
            }
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
