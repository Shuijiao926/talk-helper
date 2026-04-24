package com.talkhelper.textpreprocess.strategy.chunk;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.textpreprocess.dto.ThTextChunkConfig;
import com.talkhelper.textpreprocess.vo.ThTextChunkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 固定长度分块策略
 */
@Slf4j
@Component
public class ThFixedChunkStrategy implements ThTextChunkStrategy {

    @Override
    public List<ThTextChunkVO> chunk(String text, Object config) {
        ThTextChunkConfig chunkConfig = (ThTextChunkConfig) config;
        int maxChunkSize = chunkConfig.getMaxChunkSize() != null 
                ? chunkConfig.getMaxChunkSize() 
                : ThConstants.DEFAULT_CHUNK_SIZE;
        int overlapSize = chunkConfig.getOverlapSize() != null 
                ? chunkConfig.getOverlapSize() 
                : ThConstants.DEFAULT_OVERLAP_SIZE;

        log.info("开始固定长度分块, 文本长度: {}, 每块大小: {}, 重叠: {}", 
                text.length(), maxChunkSize, overlapSize);

        List<ThTextChunkVO> chunks = new ArrayList<>();
        int start = 0;
        int chunkId = 1;

        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());

            // 如果不是最后一块,尝试在句子边界处截断
            if (end < text.length()) {
                end = findSentenceBoundary(text, end);
            }

            String content = text.substring(start, end).trim();
            if (!content.isEmpty()) {
                // 生成上下文摘要
                String contextSummary = generateContextSummary(text, start, overlapSize);

                ThTextChunkVO chunk = ThTextChunkVO.builder()
                        .chunkId(chunkId++)
                        .content(content)
                        .contextSummary(contextSummary)
                        .startPosition(start)
                        .endPosition(end)
                        .build();

                chunks.add(chunk);
            }

            // 下一块的起始位置(考虑重叠)
            start = end - overlapSize;
            if (start < end) {
                start = end;
            }
        }

        log.info("固定长度分块完成, 共分为 {} 块", chunks.size());
        return chunks;
    }

    /**
     * 查找句子边界
     */
    private int findSentenceBoundary(String text, int position) {
        // 向前查找最近的句子结束符
        int searchBack = Math.max(0, position - ThConstants.SENTENCE_BOUNDARY_SEARCH_RANGE);
        for (int i = position; i > searchBack; i--) {
            char c = text.charAt(i);
            if (c == '。' || c == '！' || c == '？' || c == '.' || c == '!' || c == '?') {
                return i + 1;
            }
        }
        return position;
    }

    /**
     * 生成上下文摘要
     */
    private String generateContextSummary(String text, int position, int overlapSize) {
        int start = Math.max(0, position - overlapSize);
        return text.substring(start, position).trim();
    }

    @Override
    public String getStrategyName() {
        return "fixed";
    }
}
