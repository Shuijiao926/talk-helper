package com.talkhelper.textpreprocess.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 播客脚本提示词模板服务
 * 负责加载和管理提示词模板
 */
@Slf4j
@Service
public class ThPodcastPromptTemplateService {

    private static final String TEMPLATE_PATH = "podcast-templates/";
    
    // 缓存已加载的模板
    private final Map<String, String> templateCache = new HashMap<>();

    /**
     * 获取标准播客脚本提示词模板
     */
    public String getStandardTemplate() {
        return loadTemplate("template_standard.md");
    }

    /**
     * 获取深度解析播客脚本提示词模板
     */
    public String getDeepDiveTemplate() {
        return loadTemplate("template_deep_dive.md");
    }

    /**
     * 获取快速技巧播客脚本提示词模板
     */
    public String getQuickTipsTemplate() {
        return loadTemplate("template_quick_tips.md");
    }

    /**
     * 获取故事叙述播客脚本提示词模板
     */
    public String getStorytellingTemplate() {
        return loadTemplate("template_storytelling.md");
    }

    /**
     * 获取观点辩论播客脚本提示词模板
     */
    public String getDebateTemplate() {
        return loadTemplate("template_debate.md");
    }

    /**
     * 加载模板文件
     */
    private String loadTemplate(String templateName) {
        return templateCache.computeIfAbsent(templateName, name -> {
            try {
                String fullPath = TEMPLATE_PATH + name;
                var resource = getClass().getClassLoader().getResourceAsStream(fullPath);
                
                if (resource == null) {
                    log.warn("模板文件不存在: {}, 使用默认模板", fullPath);
                    return getDefaultTemplate();
                }
                
                String content = new String(resource.readAllBytes(), StandardCharsets.UTF_8);
                log.debug("成功加载模板: {}", name);
                return content;
                
            } catch (IOException e) {
                log.error("加载模板失败: {}", name, e);
                return getDefaultTemplate();
            }
        });
    }

    /**
     * 默认模板（兜底）
     */
    private String getDefaultTemplate() {
        return """
                你是一位专业的播客制作人，擅长将任何文本转换为生动有趣的双人对话播客。
                
                请将以下文本转换为时长约5分钟的播客脚本，要求：
                
                ### 1. 角色设定
                - **主持人**：活泼开朗、善于提问、代表听众视角
                - **嘉宾**：专业严谨、深入浅出、提供权威解答
                
                ### 2. 结构要求
                - **开场白**（30秒）：引入主题、介绍嘉宾、激发兴趣
                - **核心内容**（4分钟）：分2-3个知识点讲解，循序渐进
                - **结束语**（30秒）：总结要点、预告下期、感谢收听
                
                ### 3. 标注规范
                - **角色**：[主持人]、[嘉宾]
                - **语气**：(平静)、(兴奋)、(严肃)、(疑问)、(幽默)、(感慨)
                - **停顿**：[停顿1秒]、[停顿2秒]、[停顿3秒]
                - **音效**：[背景音乐：轻快钢琴曲]、[音效：翻书声]
                
                ### 4. 输出格式
                # 播客脚本：{标题}
                
                ## 开场白
                [背景音乐：轻快钢琴曲，音量20%]
                [主持人]：(兴奋) 大家好，欢迎收听今天的节目...
                
                ## 核心内容
                ...
                
                ## 结束语
                [背景音乐：渐弱]
                [主持人]：今天的节目就到这里，我们下期再见。
                
                ---
                
                ## 输入文本
                {input_text}
                """;
    }

    /**
     * 替换模板中的变量
     */
    public String renderTemplate(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
