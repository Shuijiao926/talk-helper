package com.talkhelper.textpreprocess.pipeline;

import com.talkhelper.textpreprocess.dto.ThFileUploadRequest;
import com.talkhelper.textpreprocess.vo.ThPreprocessResultVO;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文本处理管道上下文
 * 在管道的各个阶段之间传递数据
 */
@Data
@Builder
public class ThTextProcessContext {

    /**
     * 原始输入 - 文件
     */
    private MultipartFile file;

    /**
     * 原始输入 - 纯文本（语音识别或直接输入）
     */
    private String rawText;

    /**
     * 请求配置
     */
    private ThFileUploadRequest request;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件路径（临时保存路径）
     */
    private String filePath;

    /**
     * 解析后的原始文本
     */
    private String extractedText;

    /**
     * 清洗后的文本
     */
    private String cleanedText;

    /**
     * 分块后的文本列表
     */
    private List<TextChunk> chunks;

    /**
     * AI处理后的结果
     */
    private String aiResult;

    /**
     * AI结果清洗后的内容
     */
    private String cleanedAiResult;

    /**
     * 最终处理结果
     */
    private ThPreprocessResultVO result;

    /**
     * 扩展属性，用于各阶段传递自定义数据
     */
    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 进度回调（可选）
     */
    private ThProgressCallback progressCallback;

    /**
     * 任务ID（用于进度上报）
     */
    private String taskId;

    /**
     * 设置扩展属性
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取扩展属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 文本块对象
     */
    @Data
    @Builder
    public static class TextChunk {
        /**
         * 块索引
         */
        private Integer index;

        /**
         * 块内容
         */
        private String content;

        /**
         * 块长度
         */
        private Integer length;

        /**
         * 元数据（如章节标题等）
         */
        private Map<String, Object> metadata;
    }
}
