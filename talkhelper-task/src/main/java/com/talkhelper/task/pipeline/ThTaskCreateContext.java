package com.talkhelper.task.pipeline;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 任务创建上下文
 * 在Handler之间传递数据
 */
@Data
@Builder
public class ThTaskCreateContext {

    /**
     * 上传的文件
     */
    private MultipartFile file;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 请求数据JSON
     */
    private String requestData;

    /**
     * 输入文件名
     */
    private String inputFileName;

    /**
     * 输入文件MinIO URL
     */
    private String inputFileUrl;

    /**
     * 输入文件大小
     */
    private Long inputFileSize;

    /**
     * 生成的任务ID
     */
    private String taskId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否成功
     */
    @Builder.Default
    private boolean success = false;
}
