package com.talkhelper.audio.pipeline.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import com.roamingguide.starter.tts.TtsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 步骤1：脚本 JSON 解析处理器
 * 解析 LLM 生成的 JSON 脚本，支持多块拼接格式（以 "--- 分块分隔 ---" 分隔）
 *
 * 合并策略（两层）：
 * 1. 连续同角色合并 — 跨分块边界的同角色条目合并为一条
 * 2. 文本长度聚合 — 连续同角色的短条目聚合至 maxStringLength*80% 上限，不截断任何原始条目
 *
 * 支持的输入格式：
 * 1. 纯数组: [{"role":"host","text":"..."}]
 * 2. 包装对象: {"script": [{"role":"host","text":"..."}]}
 * 3. 多块拼接: 上述两种格式通过 "--- 分块分隔 ---" 拼接
 */
@Slf4j
@RequiredArgsConstructor
public class ThScriptParseHandler implements ThAudioProcessHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    private static final String CHUNK_SEPARATOR = "--- 分块分隔 ---";
    private static final double MERGE_THRESHOLD_RATIO = 0.8;
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(?:json)?\\s*\\n?(.*?)\\n?```", Pattern.DOTALL);
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("(\\[\\s*\\{.*}\\s*])", Pattern.DOTALL);
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("(\\{\\s*\".*})", Pattern.DOTALL);

    private final TtsProperties ttsProperties;

    @Override
    public String getName() {
        return "脚本JSON解析处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getPodcastScript() != null && context.getScriptItems() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始解析播客脚本JSON", getName());

        String rawScript = context.getPodcastScript().trim();

        // 1. 按分块分隔符拆分，逐块解析
        String[] chunks = rawScript.split(CHUNK_SEPARATOR);
        List<ThAudioProcessContext.ScriptItem> allItems = new ArrayList<>();
        int globalIndex = 0;

        for (int chunkIdx = 0; chunkIdx < chunks.length; chunkIdx++) {
            String chunk = chunks[chunkIdx].trim();
            if (chunk.isEmpty()) {
                continue;
            }

            log.debug("[{}] 解析第 {}/{} 个分块", getName(), chunkIdx + 1, chunks.length);

            List<Map<String, String>> rawItems = parseChunk(chunk);
            for (Map<String, String> raw : rawItems) {
                String text = raw.getOrDefault("text", "");
                if (text == null || text.isBlank()) {
                    continue;
                }
                allItems.add(ThAudioProcessContext.ScriptItem.builder()
                        .index(globalIndex++)
                        .role(raw.getOrDefault("role", "host"))
                        .text(text.trim())
                        .build());
            }
        }

        if (allItems.isEmpty()) {
            throw new IllegalArgumentException("脚本内容为空，未解析到任何有效条目");
        }

        int rawCount = allItems.size();

        // 2. 合并连续同角色条目
        List<ThAudioProcessContext.ScriptItem> merged = mergeConsecutiveSameRole(allItems);
        int afterRoleMerge = merged.size();

        // 3. 按 TTS 文本长度阈值进一步聚合同角色条目
        int mergeThreshold = (int) (ttsProperties.getMaxStringLength() * MERGE_THRESHOLD_RATIO);
        List<ThAudioProcessContext.ScriptItem> aggregated = aggregateByTextLength(merged, mergeThreshold);

        context.setScriptItems(aggregated);

        log.info("[{}] 脚本解析完成, {} 个分块, 原始 {} 条 → 同角色合并 {} 条 → 长度聚合 {} 条 (阈值={}字符)",
                getName(), chunks.length, rawCount, afterRoleMerge, aggregated.size(), mergeThreshold);

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 10, "脚本解析完成");
        }
    }

    /**
     * 合并连续同角色条目
     * 例：[host, host, guest, guest, host] → [host(合并), guest(合并), host]
     */
    private List<ThAudioProcessContext.ScriptItem> mergeConsecutiveSameRole(
            List<ThAudioProcessContext.ScriptItem> items) {

        if (items.size() <= 1) {
            return items;
        }

        List<ThAudioProcessContext.ScriptItem> merged = new ArrayList<>();
        ThAudioProcessContext.ScriptItem current = items.get(0);

        for (int i = 1; i < items.size(); i++) {
            ThAudioProcessContext.ScriptItem next = items.get(i);
            if (current.getRole().equals(next.getRole())) {
                current = ThAudioProcessContext.ScriptItem.builder()
                        .index(current.getIndex())
                        .role(current.getRole())
                        .text(current.getText() + "\n" + next.getText())
                        .build();
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        reindex(merged);
        return merged;
    }

    /**
     * 按文本长度阈值聚合连续同角色条目
     *
     * 遍历列表，当相邻条目角色相同且合并后文本长度不超过阈值时，合并为一条。
     * 单条原始文本超过阈值的，保持原样不截断。
     *
     * 注意：经过 mergeConsecutiveSameRole 后，相邻条目通常角色交替。
     * 但由于播客脚本可能存在 host→guest→host→guest 的交替模式中
     * 每条文本都很短（如 30 字），此方法不会跨角色合并，
     * 仅合并同角色条目。实际效果取决于脚本结构。
     */
    private List<ThAudioProcessContext.ScriptItem> aggregateByTextLength(
            List<ThAudioProcessContext.ScriptItem> items, int threshold) {

        if (items.size() <= 1 || threshold <= 0) {
            return items;
        }

        List<ThAudioProcessContext.ScriptItem> result = new ArrayList<>();
        ThAudioProcessContext.ScriptItem current = items.get(0);

        for (int i = 1; i < items.size(); i++) {
            ThAudioProcessContext.ScriptItem next = items.get(i);

            if (current.getRole().equals(next.getRole())) {
                int combinedLength = current.getText().length() + 1 + next.getText().length();
                if (combinedLength <= threshold) {
                    // 合并后不超过阈值，聚合
                    current = ThAudioProcessContext.ScriptItem.builder()
                            .index(current.getIndex())
                            .role(current.getRole())
                            .text(current.getText() + "\n" + next.getText())
                            .build();
                    continue;
                }
            }

            // 角色不同 或 合并后超过阈值，保存当前条目
            result.add(current);
            current = next;
        }
        result.add(current);

        reindex(result);
        return result;
    }

    private void reindex(List<ThAudioProcessContext.ScriptItem> items) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setIndex(i);
        }
    }

    /**
     * 解析单个 JSON 块
     * 支持纯数组和 {"script": [...]} 包装对象两种格式
     * 加入容错：从 markdown 代码块、混杂文本中提取 JSON
     */
    private List<Map<String, String>> parseChunk(String chunk) throws Exception {
        String cleaned = extractJson(chunk);

        if (cleaned.startsWith("[")) {
            return MAPPER.readValue(cleaned, new TypeReference<>() {});
        } else if (cleaned.startsWith("{")) {
            Map<String, Object> wrapper = MAPPER.readValue(cleaned, new TypeReference<>() {});
            Object scriptObj = wrapper.get("script");
            if (scriptObj == null) {
                // 尝试其他常见 key
                for (String key : List.of("scripts", "data", "content", "dialogue", "dialogues")) {
                    scriptObj = wrapper.get(key);
                    if (scriptObj != null) break;
                }
            }
            if (scriptObj == null) {
                throw new IllegalArgumentException("JSON 对象中未找到脚本数组字段，可用的 key: " + wrapper.keySet());
            }
            return MAPPER.convertValue(scriptObj, new TypeReference<>() {});
        } else {
            log.warn("[{}] 跳过无法识别的分块内容: {}...",
                    getName(), chunk.substring(0, Math.min(100, chunk.length())));
            return List.of();
        }
    }

    /**
     * 从 LLM 原始输出中提取 JSON 内容
     * 处理常见问题：markdown 代码块、前后多余文本、尾逗号等
     */
    private String extractJson(String raw) {
        String text = raw.trim();

        // 1. 从 markdown 代码块中提取（```json ... ``` 或 ``` ... ```）
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(text);
        if (codeBlockMatcher.find()) {
            text = codeBlockMatcher.group(1).trim();
            log.debug("[{}] 从 markdown 代码块中提取 JSON", getName());
        }

        // 2. 如果首字符已经是 [ 或 {，直接返回
        if (text.startsWith("[") || text.startsWith("{")) {
            return text;
        }

        // 3. 尝试从混杂文本中提取 JSON 数组
        Matcher arrayMatcher = JSON_ARRAY_PATTERN.matcher(text);
        if (arrayMatcher.find()) {
            log.debug("[{}] 从混杂文本中提取 JSON 数组", getName());
            return arrayMatcher.group(1);
        }

        // 4. 尝试从混杂文本中提取 JSON 对象
        Matcher objectMatcher = JSON_OBJECT_PATTERN.matcher(text);
        if (objectMatcher.find()) {
            log.debug("[{}] 从混杂文本中提取 JSON 对象", getName());
            return objectMatcher.group(1);
        }

        // 5. 无法提取，返回原文（后续解析会报错）
        return text;
    }
}
