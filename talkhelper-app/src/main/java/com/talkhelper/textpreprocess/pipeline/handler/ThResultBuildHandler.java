package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class ThResultBuildHandler implements ThTextProcessHandler {

    private final ThObjectStorageGateway storageGateway;

    @Override
    public String getName() {
        return "结果构建处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        return true;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始构建结果", getName());

        String originalFileName = context.getFile() != null
                ? context.getFile().getOriginalFilename()
                : ThConstants.DEFAULT_INPUT_FILENAME;

        String fileType = context.getFileType() != null
                ? context.getFileType()
                : ThConstants.DEFAULT_FILE_TYPE;

        int textLength = context.getCleanedText() != null ? context.getCleanedText().length() : 0;
        int chunkCount = context.getChunks() != null ? context.getChunks().size() : 0;

        String podcastScript = context.getAiResult();
        String outputFileName = null;
        String outputFileUrl = null;

        if (podcastScript != null && !podcastScript.isEmpty()) {
            try {
                String baseName = originalFileName.contains(".")
                        ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                        : originalFileName;
                outputFileName = baseName + "_podcast_script.txt";

                byte[] contentBytes = podcastScript.getBytes(StandardCharsets.UTF_8);
                MultipartFile scriptFile = new com.talkhelper.common.util.ThSimpleMultipartFile(
                        "file",
                        outputFileName,
                        "text/plain",
                        contentBytes
                );

                String taskId = context.getTaskId();
                if (taskId == null || taskId.isEmpty()) {
                    taskId = "default-" + System.currentTimeMillis();
                }
                String objectKey = "task/" + taskId + "/" + outputFileName;
                outputFileUrl = storageGateway.uploadFile(objectKey, scriptFile);

                log.info("[{}] Podcast script uploaded: {}", getName(), outputFileUrl);
            } catch (Exception e) {
                log.error("[{}] Failed to upload podcast script", getName(), e);
            }
        }

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
        log.info("[{}] 结果构建完成: file={}, textLength={}, chunkCount={}, output={}",
                getName(), originalFileName, textLength, chunkCount, outputFileName);
    }
}
