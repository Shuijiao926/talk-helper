package com.talkhelper.textpreprocess.strategy.cleaner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 通用文本清洗策略
 * 删除无关内容、多余空白、特殊字符等
 */
@Slf4j
@Component
public class ThGeneralCleanerStrategy implements ThTextCleanerStrategy {

    private static final Pattern MULTIPLE_SPACES = Pattern.compile("\\s{2,}");
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("\\n{3,}");

    @Override
    public String clean(String text) {
        log.info("开始通用文本清洗, 原始长度: {}", text.length());

        String cleaned = text;

        // 1. 删除页眉页脚常见模式
        cleaned = removeHeadersFooters(cleaned);

        // 2. 删除参考文献
        cleaned = removeReferences(cleaned);

        // 3. 删除目录
        cleaned = removeTableOfContents(cleaned);

        // 4. 删除广告和无关链接
        cleaned = removeAdvertisements(cleaned);

        // 5. 标准化空白字符
        cleaned = normalizeWhitespace(cleaned);

        // 6. 删除多余空行
        cleaned = removeExtraNewlines(cleaned);

        log.info("文本清洗完成, 清洗后长度: {}", cleaned.length());
        return cleaned.trim();
    }

    /**
     * 删除页眉页脚
     */
    private String removeHeadersFooters(String text) {
        // 删除常见的页眉页脚模式
        text = text.replaceAll("(?m)^\\s*第\\s*\\d+\\s*页.*$", "");
        text = text.replaceAll("(?m)^\\s*Page\\s*\\d+.*$", "");
        return text;
    }

    /**
     * 删除参考文献
     */
    private String removeReferences(String text) {
        // 删除参考文献部分
        text = text.replaceAll("(?i)(参考文献|References|Bibliography)\\s*[:：].*?(?=\\n\\n|$)", "");
        // 删除引用标记 [1], [2] 等
        text = text.replaceAll("\\[\\d+\\]", "");
        return text;
    }

    /**
     * 删除目录
     */
    private String removeTableOfContents(String text) {
        // 简单删除目录部分(可根据实际情况优化)
        text = text.replaceAll("(?i)(目录|Contents|Table of Contents)\\s*[:：].*?(?=\\n\\n\\n|$)", "");
        return text;
    }

    /**
     * 删除广告
     */
    private String removeAdvertisements(String text) {
        // 删除常见广告模式
        text = text.replaceAll("(?i)(广告|Advertisement|Sponsored).*?(?=\\n\\n|$)", "");
        return text;
    }

    /**
     * 标准化空白字符
     */
    private String normalizeWhitespace(String text) {
        // 将多个空格替换为单个空格
        text = MULTIPLE_SPACES.matcher(text).replaceAll(" ");
        // 删除每行首尾空白
        text = text.replaceAll("(?m)^\\s+|\\s+$", "");
        return text;
    }

    /**
     * 删除多余空行
     */
    private String removeExtraNewlines(String text) {
        // 将3个或更多连续换行符替换为2个
        return MULTIPLE_NEWLINES.matcher(text).replaceAll("\n\n");
    }

    @Override
    public String getStrategyName() {
        return "general";
    }
}
