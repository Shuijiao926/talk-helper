package com.talkhelper.textpreprocess.dto;

import com.talkhelper.common.enums.ThContentSaveStrategy;
import com.talkhelper.common.enums.ThFileType;
import com.talkhelper.common.enums.ThTextChunkStrategy;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传请求DTO
 */
@Data
@Builder
public class ThFileUploadRequest {

    /**
     * 上传的文件
     */
    private MultipartFile file;

    /**
     * 输入文件MinIO URL(与file二选一)
     */
    private String inputFileUrl;

    /**
     * 文件类型: txt, md, html, pdf, docx, epub
     */
    private ThFileType fileType;

    /**
     * 是否进行口语化处理
     */
    @Builder.Default
    private Boolean colloquialize = true;

    /**
     * 是否进行AI结构化清洗(添加Markdown标题)
     */
    @Builder.Default
    private Boolean aiStructured = true;

    /**
     * 分块策略: chapter, paragraph, fixed
     */
    @Builder.Default
    private ThTextChunkStrategy chunkStrategy = ThTextChunkStrategy.FIXED;

    /**
     * 每块最大字符数(仅在fixed策略下生效)
     */
    @Builder.Default
    private Integer maxChunkSize = 2000;

    /**
     * 保存策略列表: 支持同时保存到多个位置
     * 例如: [LOCAL_FILE, VECTOR_DB] 表示同时保存到本地和向量数据库
     */
    @Builder.Default
    private ThContentSaveStrategy[] saveStrategies = {ThContentSaveStrategy.DATABASE};
}
