package com.talkhelper.audio.pipeline.handler;

import com.talkhelper.audio.pipeline.ThAudioProcessContext;
import com.talkhelper.audio.pipeline.ThAudioProcessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 步骤1：脚本结构化解析处理器
 * 解析带标注的播客脚本，提取角色、情绪、停顿、BGM、音效等标签
 */
@Slf4j
@Component
public class ThScriptParseHandler implements ThAudioProcessHandler {

    // 正则表达式模式
    private static final Pattern ROLE_PATTERN = Pattern.compile("\\[(主持人|嘉宾|旁白)\\]");
    private static final Pattern EMOTION_PATTERN = Pattern.compile("\\((轻快|严肃|慢速|快速|激动|平静)\\)");
    private static final Pattern PAUSE_PATTERN = Pattern.compile("\\[停顿([0-9.]+)秒\\]");
    private static final Pattern BGM_PATTERN = Pattern.compile("\\[背景音乐：([^\\]]+)\\]");
    private static final Pattern SFX_PATTERN = Pattern.compile("\\[音效：([^\\]]+)\\]");

    @Override
    public String getName() {
        return "脚本结构化解析处理器";
    }

    @Override
    public boolean shouldHandle(ThAudioProcessContext context) {
        return context.getPodcastScript() != null && context.getSegments() == null;
    }

    @Override
    public void handle(ThAudioProcessContext context) throws Exception {
        log.info("[{}] 开始解析播客脚本", getName());
        
        String script = context.getPodcastScript();
        List<ThAudioProcessContext.PodcastSegment> segments = parseScript(script);
        
        context.setSegments(segments);
        
        log.info("[{}] 脚本解析完成, 共 {} 个段落", getName(), segments.size());
        
        // 上报进度
        if (context.getProgressCallback() != null && context.getTaskId() != null) {
            context.getProgressCallback().updateProgress(context.getTaskId(), 14, "脚本解析完成");
        }
    }

    /**
     * 解析脚本为结构化段落
     */
    private List<ThAudioProcessContext.PodcastSegment> parseScript(String script) {
        List<ThAudioProcessContext.PodcastSegment> segments = new ArrayList<>();
        
        // 按行分割
        String[] lines = script.split("\n");
        
        int index = 0;
        String currentRole = "主持人"; // 默认角色
        String currentEmotion = "平静"; // 默认情绪
        String currentBgm = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            // 检测BGM标签
            Matcher bgmMatcher = BGM_PATTERN.matcher(line);
            if (bgmMatcher.find()) {
                currentBgm = bgmMatcher.group(1);
                continue;
            }
            
            // 检测角色标签
            Matcher roleMatcher = ROLE_PATTERN.matcher(line);
            if (roleMatcher.find()) {
                currentRole = roleMatcher.group(1);
            }
            
            // 检测情绪标签
            Matcher emotionMatcher = EMOTION_PATTERN.matcher(line);
            if (emotionMatcher.find()) {
                currentEmotion = emotionMatcher.group(1);
            }
            
            // 检测停顿标签
            Matcher pauseMatcher = PAUSE_PATTERN.matcher(line);
            Double pauseDuration = 0.0;
            if (pauseMatcher.find()) {
                pauseDuration = Double.parseDouble(pauseMatcher.group(1));
            }
            
            // 检测音效标签
            Matcher sfxMatcher = SFX_PATTERN.matcher(line);
            String sfxTag = null;
            if (sfxMatcher.find()) {
                sfxTag = sfxMatcher.group(1);
            }
            
            // 提取纯文本（移除所有标签）
            String pureText = extractPureText(line);
            
            if (!pureText.isEmpty()) {
                ThAudioProcessContext.PodcastSegment segment = ThAudioProcessContext.PodcastSegment.builder()
                        .index(index++)
                        .role(currentRole)
                        .emotion(currentEmotion)
                        .text(pureText)
                        .pauseDuration(pauseDuration)
                        .bgmTag(currentBgm)
                        .sfxTag(sfxTag)
                        .rawContent(line)
                        .build();
                
                segments.add(segment);
            }
        }
        
        return segments;
    }

    /**
     * 提取纯文本（移除所有标签）
     */
    private String extractPureText(String line) {
        String text = line;
        // 移除角色标签
        text = text.replaceAll("\\[(主持人|嘉宾|旁白)\\]", "");
        // 移除情绪标签
        text = text.replaceAll("\\((轻快|严肃|慢速|快速|激动|平静)\\)", "");
        // 移除停顿标签
        text = text.replaceAll("\\[停顿[0-9.]+秒\\]", "");
        // 移除BGM标签
        text = text.replaceAll("\\[背景音乐：[^\\]]+\\]", "");
        // 移除音效标签
        text = text.replaceAll("\\[音效：[^\\]]+\\]", "");
        
        return text.trim();
    }
}
