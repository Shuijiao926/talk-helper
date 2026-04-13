package com.talkhelper.textpreprocess.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本预处理结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThPreprocessResultVO {

    /**
     * 原始文件名
     */
    private String originalFileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 提取的纯文本
     */
    private String extractedText;

    /**
     * 清洗后的文本
     */
    private String cleanedText;

    /**
     * 文本总长度
     */
    private Integer textLength;

    /**
     * 分块数量
     */
    private Integer chunkCount;

    /**
     * AI处理后的播客脚本
     */
    private String aiResult;

    /**
     * 输出文件名(播客脚本文本文件)
     */
    private String outputFileName;

    /**
     * 输出文件MinIO URL
     */
    private String outputFileUrl;

    /**
     * 处理状态: success, failed
     */
    private String status;

    /**
     * 错误信息(如果失败)
     */
    private String errorMessage;
}
