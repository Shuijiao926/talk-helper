package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThConstants;
import com.roamingguide.starter.storage.ObjectStorageFactory;
import com.roamingguide.starter.storage.ObjectStorageStrategy;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 结果构建处理器
 * 负责将处理过程中的数据组装为最终结果
 * 注意：此处理器仅负责构建结果对象，不负责内容保存
 * 内容保存应由调用方根据需要独立调用保存服务
 */
@Slf4j
@RequiredArgsConstructor
public class ThResultBuildHandler implements ThTextProcessHandler {

    private final ObjectStorageFactory storageFactory;

    @Override
    public String getName() {
        return "结果构建处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        // 总是执行，用于构建最终结果
        return true;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始构建结果", getName());

        // 确定原始文件名
        String originalFileName = context.getFile() != null 
                ? context.getFile().getOriginalFilename() 
                : ThConstants.DEFAULT_INPUT_FILENAME;

        // 确定文件类型
        String fileType = context.getFileType() != null 
                ? context.getFileType() 
                : ThConstants.DEFAULT_FILE_TYPE;

        // 确定文本长度和分块数量
        int textLength = context.getCleanedText() != null ? context.getCleanedText().length() : 0;
        int chunkCount = context.getChunks() != null ? context.getChunks().size() : 0;

        // 获取AI处理后的播客文本
        String podcastScript = context.getAiResult();
        
        // 如果有AI处理结果，保存到MinIO
        String outputFileName = null;
        String outputFileUrl = null;
        Long outputFileSize = null;
        
        if (podcastScript != null && !podcastScript.isEmpty()) {
            try {
                // 生成输出文件名
                String baseName = originalFileName.contains(".") 
                        ? originalFileName.substring(0, originalFileName.lastIndexOf('.')) 
                        : originalFileName;
                outputFileName = baseName + "_podcast_script.txt";
                
                // 转换为MultipartFile
                byte[] contentBytes = podcastScript.getBytes(StandardCharsets.UTF_8);
                MultipartFile scriptFile = new com.talkhelper.common.util.ThSimpleMultipartFile(
                        "file", 
                        outputFileName, 
                        "text/plain", 
                        contentBytes
                );
                
                // 上传到MinIO
                ObjectStorageStrategy storage = storageFactory.getActiveStorage();
                String taskId = context.getTaskId();
                if (taskId == null || taskId.isEmpty()) {
                    taskId = "default-" + System.currentTimeMillis();
                }
                String objectKey = "task/" + taskId + "/" + outputFileName;
                String bucketName = "talkhelper"; // TODO: 从配置读取
                
                outputFileUrl = storage.uploadFile(scriptFile, bucketName, objectKey);
                outputFileSize = (long) contentBytes.length;
                
                log.info("[{}] 播客脚本已上传到MinIO: {}", getName(), outputFileUrl);
                
            } catch (Exception e) {
                log.error("[{}] 播客脚本上传MinIO失败", getName(), e);
                // 不抛出异常，允许流程继续，只是没有输出文件
            }
        }

        // 构建结果对象
        ThPreprocessResultVO result = ThPreprocessResultVO.builder()
                .originalFileName(originalFileName)
                .fileType(fileType)
                .extractedText(context.getExtractedText())
                .cleanedText(context.getCleanedText())
                .textLength(textLength)
                .chunkCount(chunkCount)
                .aiResult(podcastScript)
                .outputFileName(outputFileName)
                .outputFileUrl(outputFileUrl)
                .status(ThConstants.STATUS_SUCCESS)
                .build();

        context.setResult(result);
        
        log.info("[{}] 结果构建完成: 文件名={}, 文本长度={}, 分块数={}, 输出文件={}", 
                getName(), originalFileName, textLength, chunkCount, outputFileName);
    }
}
