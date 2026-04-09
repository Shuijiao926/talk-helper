package com.talkhelper.textpreprocess.dto;

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
     * 文件类型: txt, md, html, pdf, docx, epub
     */
    private String fileType;

    /**
     * 是否进行口语化处理
     */
    private Boolean colloquialize = true;

    /**
     * 分块策略: chapter, paragraph, fixed
     */
    private String chunkStrategy = "chapter";

    /**
     * 每块最大字符数(仅在fixed策略下生效)
     */
    private Integer maxChunkSize = 2000;
}
