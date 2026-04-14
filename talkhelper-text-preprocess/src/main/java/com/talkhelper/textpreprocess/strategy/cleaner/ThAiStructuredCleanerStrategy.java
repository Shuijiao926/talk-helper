package com.talkhelper.textpreprocess.strategy.cleaner;

import com.talkhelper.common.dto.ThLlmRequest;
import com.talkhelper.common.dto.ThLlmResponse;
import com.talkhelper.common.llm.ThLlmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI结构化清洗策略
 * 使用LLM为无结构文本添加Markdown层级标题
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThAiStructuredCleanerStrategy implements ThTextCleanerStrategy {

    private final ThLlmService llmService;

    private static final String PROMPT_TEMPLATE = """
            你是专业的文本结构化助手。
            请给下面的无结构技术文本标注Markdown层级标题，只输出结果，不要解释：
            1. 一级标题用 #
            2. 二级标题用 ##
            3. 保留原文的重点内容，去除冗余表达进行精炼，合理的增删
            4. 不要输出代码块标记
            
            原文：
            {text}
            """;

    @Override
    public String clean(String text) {
        log.info("开始AI结构化清洗, 原始长度: {}", text.length());

        try {
            // 构建变量映射
            Map<String, String> variables = new java.util.HashMap<>();
            variables.put("text", text);

            // 调用LLM
            ThLlmRequest request = ThLlmRequest.builder()
                    .promptTemplate(PROMPT_TEMPLATE)
                    .variables(variables)
                    .build();

            ThLlmResponse response = llmService.execute(request);

            if (!response.isSuccess()) {
                log.warn("AI结构化清洗失败: {}, 返回原文本", response.getErrorMessage());
                return text;
            }

            String structuredText = response.getContent();

            // 清理可能的代码块标记
            structuredText = structuredText.replaceAll("```markdown\\n?", "");
            structuredText = structuredText.replaceAll("```\\n?", "");
            structuredText = structuredText.trim();

            log.info("AI结构化清洗完成, 原始长度: {}, 结构化后长度: {}", 
                    text.length(), structuredText.length());

            return structuredText;

        } catch (Exception e) {
            log.error("AI结构化清洗异常, 返回原文本", e);
            return text;
        }
    }

    @Override
    public String getStrategyName() {
        return "ai-structured";
    }
}
