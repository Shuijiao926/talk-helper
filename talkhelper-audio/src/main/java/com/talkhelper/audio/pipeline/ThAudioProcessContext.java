package com.talkhelper.audio.pipeline;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 音频处理管道上下文
 * 在管道的各个阶段之间传递数据
 */
@Data
@Builder
public class ThAudioProcessContext {

    /**
     * 输入：格式化播客脚本（带标注）
     */
    private String podcastScript;

    /**
     * 步骤1输出：解析后的结构化段落列表
     */
    private List<PodcastSegment> segments;

    /**
     * 步骤2输出：分段后的短文本片段（100-300字/段）
     */
    private List<TextFragment> fragments;

    /**
     * 步骤3输出：TTS合成的音频片段路径列表
     */
    private List<String> ttsAudioPaths;

    /**
     * 步骤4输出：插入停顿后的音频片段路径列表
     */
    private List<String> pausedAudioPaths;

    /**
     * 步骤5输出：完整人声音频路径
     */
    private String fullVocalPath;

    /**
     * 步骤6输出：混音后的音频路径（BGM+音效+人声）
     */
    private String mixedAudioPath;

    /**
     * 步骤7输出：最终MP3文件路径
     */
    private String finalMp3Path;

    /**
     * 最终结果：音频URL（对象存储地址）
     */
    private String audioUrl;

    /**
     * 音频元数据
     */
    private AudioMetadata metadata;

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
     * 播客段落对象（步骤1输出）
     */
    @Data
    @Builder
    public static class PodcastSegment {
        /**
         * 段落索引
         */
        private Integer index;

        /**
         * 角色（主持人/嘉宾）
         */
        private String role;

        /**
         * 情绪/语速（轻快/严肃/慢速）
         */
        private String emotion;

        /**
         * 纯朗读文本
         */
        private String text;

        /**
         * 停顿时长（秒）
         */
        private Double pauseDuration;

        /**
         * BGM标签
         */
        private String bgmTag;

        /**
         * 音效标签
         */
        private String sfxTag;

        /**
         * 原始段落内容
         */
        private String rawContent;
    }

    /**
     * 文本片段对象（步骤2输出）
     */
    @Data
    @Builder
    public static class TextFragment {
        /**
         * 片段索引
         */
        private Integer index;

        /**
         * 所属段落索引
         */
        private Integer segmentIndex;

        /**
         * 角色
         */
        private String role;

        /**
         * 情绪
         */
        private String emotion;

        /**
         * 文本内容（100-300字）
         */
        private String text;

        /**
         * 文本长度
         */
        private Integer length;

        /**
         * 停顿时长（秒）
         */
        private Double pauseDuration;
    }

    /**
     * 音频元数据
     */
    @Data
    @Builder
    public static class AudioMetadata {
        /**
         * 音频时长（秒）
         */
        private Double duration;

        /**
         * 文件大小（字节）
         */
        private Long fileSize;

        /**
         * 文件格式（mp3/wav）
         */
        private String format;

        /**
         * 比特率（kbps）
         */
        private Integer bitrate;

        /**
         * 采样率（Hz）
         */
        private Integer sampleRate;

        /**
         * 文件哈希（SHA256）
         */
        private String fileHash;

        /**
         * 创建时间
         */
        private Long createTime;
    }
}
