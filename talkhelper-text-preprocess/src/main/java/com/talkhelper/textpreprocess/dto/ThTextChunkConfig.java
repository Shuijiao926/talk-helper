package com.talkhelper.textpreprocess.dto;

import lombok.Data;

/**
 * 文本分块配置DTO
 */
@Data
public class ThTextChunkConfig {

    /**
     * 分块策略: chapter(按章节), paragraph(按段落), fixed(固定长度)
     */
    private String strategy = "chapter";

    /**
     * 每块最大字符数
     */
    private Integer maxChunkSize = 2000;

    /**
     * 块之间重叠字符数(保持上下文连贯)
     */
    private Integer overlapSize = 200;

    /**
     * 是否保留上下文摘要
     */
    private Boolean keepContextSummary = true;
}
