package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.enums.ThContentSaveStrategy;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.strategy.saver.ThContentSaverFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThMultiStrategySaverExecutor;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 结果构建处理器
 * 负责将处理过程中的数据组装为最终结果
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThResultBuildHandler implements ThTextProcessHandler {

    private final ThContentSaverFactory saverFactory;
    private final ThMultiStrategySaverExecutor multiStrategyExecutor;

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

        // 构建结果对象
        ThPreprocessResultVO result = ThPreprocessResultVO.builder()
                .originalFileName(originalFileName)
                .fileType(fileType)
                .extractedText(context.getExtractedText())
                .cleanedText(context.getCleanedText())
                .textLength(textLength)
                .chunkCount(chunkCount)
                .status(ThConstants.STATUS_SUCCESS)
                .build();

        context.setResult(result);
        
        // 如果需要保存分块后的文本到多个位置，使用多策略执行器
        if (context.getChunks() != null && !context.getChunks().isEmpty()) {
            ThContentSaveStrategy[] strategies = context.getRequest() != null 
                    ? context.getRequest().getSaveStrategies() 
                    : new ThContentSaveStrategy[]{ThContentSaveStrategy.LOCAL_FILE};
            
            // 使用带过滤的执行器，跳过本地文件策略（文件已在 ThFileSaveHandler 中保存）
            multiStrategyExecutor.executeWithFilter(
                    strategies,
                    saver -> {
                        log.info("开始保存 {} 个文本块", context.getChunks().size());
                        context.getChunks().forEach(chunk -> saver.save(chunk.getContent(), null));
                    },
                    ThConstants.ACTION_CHUNK_TEXT_SAVE,
                    ThContentSaveStrategy.LOCAL_FILE
            );
        }
        
        log.info("[{}] 结果构建完成: 文件名={}, 文本长度={}, 分块数={}", 
                getName(), originalFileName, textLength, chunkCount);
    }
}
