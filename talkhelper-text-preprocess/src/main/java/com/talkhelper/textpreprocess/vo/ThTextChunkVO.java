package com.talkhelper.textpreprocess.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文本分块结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThTextChunkVO {

    /**
     * 分块ID
     */
    private Integer chunkId;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 章节标题(如果有)
     */
    private String chapterTitle;

    /**
     * 上下文摘要
     */
    private String contextSummary;

    /**
     * 起始位置
     */
    private Integer startPosition;

    /**
     * 结束位置
     */
    private Integer endPosition;

    /**
     * 所有分块列表
     */
    private List<ThTextChunkVO> chunks;
}
