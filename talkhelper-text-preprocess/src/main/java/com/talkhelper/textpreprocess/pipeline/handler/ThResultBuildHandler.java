package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 结果构建处理器
 * 负责将处理过程中的数据组装为最终结果
 */
@Slf4j
@Component
public class ThResultBuildHandler implements ThTextProcessHandler {

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
                : "text_input";

        // 确定文件类型
        String fileType = context.getFileType() != null 
                ? context.getFileType() 
                : "text";

        // 确定文本长度
        Integer textLength = context.getCleanedText() != null 
                ? context.getCleanedText().length() 
                : 0;

        // 确定分块数量
        Integer chunkCount = context.getChunks() != null 
                ? context.getChunks().size() 
                : 0;

        // 构建结果对象
        ThPreprocessResultVO result = ThPreprocessResultVO.builder()
                .originalFileName(originalFileName)
                .fileType(fileType)
                .extractedText(context.getExtractedText())
                .cleanedText(context.getCleanedText())
                .textLength(textLength)
                .chunkCount(chunkCount)
                .status("success")
                .build();

        context.setResult(result);
        
        log.info("[{}] 结果构建完成: 文件名={}, 文本长度={}, 分块数={}", 
                getName(), originalFileName, textLength, chunkCount);
    }
}
