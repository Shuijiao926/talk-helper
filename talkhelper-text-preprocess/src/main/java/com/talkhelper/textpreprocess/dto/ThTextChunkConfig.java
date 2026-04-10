package com.talkhelper.textpreprocess.dto;

import com.talkhelper.common.constant.ThConstants;
import com.talkhelper.common.enums.ThTextChunkStrategy;
import lombok.Builder;
import lombok.Data;

/**
 * 文本分块配置DTO
 */
@Data
@Builder
public class ThTextChunkConfig {

    /**
     * 分块策略: chapter(按章节), paragraph(按段落), fixed(固定长度)
     */
    @Builder.Default
    private ThTextChunkStrategy strategy = ThTextChunkStrategy.CHAPTER;

    /**
     * 每块最大字符数
     */
    @Builder.Default
    private Integer maxChunkSize = ThConstants.DEFAULT_CHUNK_SIZE;

    /**
     * 块之间重叠字符数(保持上下文连贯)
     */
    @Builder.Default
    private Integer overlapSize = ThConstants.DEFAULT_OVERLAP_SIZE;

    /**
     * 是否保留上下文摘要
     */
    @Builder.Default
    private Boolean keepContextSummary = true;
}
