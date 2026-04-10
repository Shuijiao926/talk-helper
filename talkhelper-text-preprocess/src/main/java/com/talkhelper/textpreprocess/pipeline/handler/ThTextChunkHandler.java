package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.enums.ThTextChunkStrategy;
import com.talkhelper.textpreprocess.dto.ThTextChunkConfig;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.strategy.chunk.ThChapterChunkStrategy;
import com.talkhelper.textpreprocess.strategy.chunk.ThFixedChunkStrategy;
import com.talkhelper.textpreprocess.vo.ThTextChunkVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文本分块处理器
 * 根据配置选择不同的分块策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThTextChunkHandler implements ThTextProcessHandler {

    private final ThFixedChunkStrategy fixedChunkStrategy;
    private final ThChapterChunkStrategy chapterChunkStrategy;

    @Override
    public String getName() {
        return "文本分块处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        // 如果有清洗后的文本且还没有分块，则需要分块
        return context.getCleanedText() != null && context.getChunks() == null;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始文本分块", getName());

        // 获取分块配置
        ThTextChunkStrategy strategy = context.getRequest() != null && context.getRequest().getChunkStrategy() != null
                ? context.getRequest().getChunkStrategy()
                : ThTextChunkStrategy.FIXED;

        // 根据策略类型选择分块器
        List<ThTextProcessContext.TextChunk> chunks;
        
        if (ThTextChunkStrategy.CHAPTER.equals(strategy)) {
            chunks = chunkByChapter(context);
        } else {
            chunks = chunkByFixed(context);
        }

        context.setChunks(chunks);
        
        log.info("[{}] 文本分块完成, 分块数量: {}", getName(), chunks.size());
    }

    /**
     * 按固定大小分块
     */
    private List<ThTextProcessContext.TextChunk> chunkByFixed(ThTextProcessContext context) {
        Integer chunkSize = context.getRequest() != null && context.getRequest().getMaxChunkSize() != null
                ? context.getRequest().getMaxChunkSize()
                : ThConstants.DEFAULT_MAX_CHUNK_SIZE;
        
        Integer overlap = ThConstants.DEFAULT_OVERLAP_SIZE;

        ThTextChunkConfig chunkConfig = ThTextChunkConfig.builder()
                .strategy(ThTextChunkStrategy.FIXED)
                .maxChunkSize(chunkSize)
                .overlapSize(overlap)
                .build();
        
        List<ThTextChunkVO> textChunkVOs = fixedChunkStrategy.chunk(context.getCleanedText(), chunkConfig);
        List<String> textChunks = textChunkVOs.stream()
                .map(ThTextChunkVO::getContent)
                .collect(Collectors.toList());
        
        return convertToTextChunks(textChunks);
    }

    /**
     * 按章节分块
     */
    private List<ThTextProcessContext.TextChunk> chunkByChapter(ThTextProcessContext context) {
        List<ThTextChunkVO> textChunkVOs = chapterChunkStrategy.chunk(context.getCleanedText(), null);
        List<String> textChunks = textChunkVOs.stream()
                .map(ThTextChunkVO::getContent)
                .collect(Collectors.toList());
        
        return convertToTextChunks(textChunks);
    }

    /**
     * 转换为TextChunk对象列表
     */
    private List<ThTextProcessContext.TextChunk> convertToTextChunks(List<String> textChunks) {
        List<ThTextProcessContext.TextChunk> chunks = new ArrayList<>();
        for (int i = 0; i < textChunks.size(); i++) {
            String content = textChunks.get(i);
            ThTextProcessContext.TextChunk chunk = ThTextProcessContext.TextChunk.builder()
                    .index(i)
                    .content(content)
                    .length(content.length())
                    .build();
            chunks.add(chunk);
        }
        return chunks;
    }
}
