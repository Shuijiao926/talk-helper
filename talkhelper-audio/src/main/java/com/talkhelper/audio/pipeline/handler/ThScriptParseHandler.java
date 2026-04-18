package com.talkhelper.audio.pipeline.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 步骤1：脚本 JSON 解析处理器
 * 解析 LLM 生成的 JSON 脚本数组 [{role, text}]
 */
@Slf4j
public class ThScriptParseHandler implements ThAudioProcessHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

        String script = context.getPodcastScript().trim();

        // 支持两种格式：
        // 1. 纯数组: [{"role":"host","text":"..."}]
        // 2. 包装对象: {"script": [{"role":"host","text":"..."}]}
        List<Map<String, String>> rawItems;
        if (script.startsWith("[")) {
            rawItems = MAPPER.readValue(script, new TypeReference<>() {});
        } else {
            Map<String, Object> wrapper = MAPPER.readValue(script, new TypeReference<>() {});
            Object scriptObj = wrapper.get("script");
            if (scriptObj == null) {
                throw new IllegalArgumentException("JSON 对象中未找到 'script' 字段");
            }
            rawItems = MAPPER.convertValue(scriptObj, new TypeReference<>() {});
        }

        if (rawItems == null || rawItems.isEmpty()) {
            throw new IllegalArgumentException("脚本内容为空");
        }

        // 转换为 ScriptItem
        List<ThAudioProcessContext.ScriptItem> scriptItems = IntStream.range(0, rawItems.size())
                .mapToObj(i -> {
                    Map<String, String> raw = rawItems.get(i);
                    return ThAudioProcessContext.ScriptItem.builder()
                            .index(i)
                            .role(raw.getOrDefault("role", "host"))
                            .text(raw.getOrDefault("text", ""))
                            .build();
                })
                .filter(item -> item.getText() != null && !item.getText().isBlank())
                .toList();

        context.setScriptItems(scriptItems);

        log.info("[{}] 脚本解析完成, 共 {} 个条目", getName(), scriptItems.size());

        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 10, "脚本解析完成");
        }
    }
}
