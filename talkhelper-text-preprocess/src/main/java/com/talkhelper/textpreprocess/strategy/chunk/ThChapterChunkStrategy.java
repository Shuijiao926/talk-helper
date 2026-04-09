package com.talkhelper.textpreprocess.strategy.chunk;

import com.talkhelper.textpreprocess.dto.ThTextChunkConfig;
import com.talkhelper.textpreprocess.vo.ThTextChunkVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 按章节分块策略
 */
@Slf4j
@Component
public class ThChapterChunkStrategy implements ThTextChunkStrategy {

    @Override
    public List<ThTextChunkVO> chunk(String text, Object config) {
        log.info("开始按章节分块, 文本长度: {}", text.length());

        List<ThTextChunkVO> chunks = new ArrayList<>();

        // 按常见章节标题分割
        String[] parts = text.split("(?=第[一二三四五六七八九十百千\\d]+章|Chapter\\s+\\d+|\\n#{1,2}\\s+)");

        int chunkId = 1;
        for (String part : parts) {
            if (part.trim().isEmpty()) continue;

            // 提取章节标题
            String chapterTitle = extractChapterTitle(part);

            ThTextChunkVO chunk = ThTextChunkVO.builder()
                    .chunkId(chunkId++)
                    .content(part.trim())
                    .chapterTitle(chapterTitle)
                    .startPosition(0)
                    .endPosition(part.length())
                    .build();

            chunks.add(chunk);
        }

        log.info("章节分块完成, 共分为 {} 块", chunks.size());
        return chunks;
    }

    /**
     * 提取章节标题
     */
    private String extractChapterTitle(String text) {
        // 匹配中文章节标题
        if (text.matches("(?s)^第[一二三四五六七八九十百千\\d]+章.*")) {
            String[] lines = text.split("\\n");
            if (lines.length > 0) {
                return lines[0].trim();
            }
        }
        // 匹配英文章节标题
        if (text.matches("(?i)^Chapter\\s+\\d+.*")) {
            String[] lines = text.split("\\n");
            if (lines.length > 0) {
                return lines[0].trim();
            }
        }
        return null;
    }

    @Override
    public String getStrategyName() {
        return "chapter";
    }
}
