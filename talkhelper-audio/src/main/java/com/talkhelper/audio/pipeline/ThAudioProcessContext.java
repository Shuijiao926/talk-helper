package com.talkhelper.audio.pipeline;

import lombok.Builder;
import lombok.Data;

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
     * 输入：播客脚本 JSON 字符串
     * 格式：[{"role":"host","text":"..."},{"role":"guest","text":"..."}]
     */
    private String podcastScript;

    /**
     * 步骤1输出：解析后的脚本条目列表
     */
    private List<ScriptItem> scriptItems;

    /**
     * 步骤2输出：TTS合成的音频片段路径列表（有序）
     */
    private List<String> ttsAudioPaths;

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

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 脚本条目（对标 TwoCast 的 ScriptItem）
     */
    @Data
    @Builder
    public static class ScriptItem {
        private Integer index;
        private String role;  // "host" / "guest"
        private String text;
    }

    /**
     * 音频元数据
     */
    @Data
    @Builder
    public static class AudioMetadata {
        private Double duration;
        private Long fileSize;
        private String format;
        private Integer bitrate;
        private Integer sampleRate;
        private String fileHash;
        private Long createTime;
    }
}
