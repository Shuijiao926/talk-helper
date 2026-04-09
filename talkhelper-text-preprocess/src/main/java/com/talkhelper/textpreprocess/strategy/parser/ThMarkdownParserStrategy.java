package com.talkhelper.textpreprocess.strategy.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Markdown文件解析器
 */
@Slf4j
@Component
public class ThMarkdownParserStrategy implements ThDocumentParserStrategy {

    @Override
    public String parse(String filePath) {
        try {
            log.info("开始解析Markdown文件: {}", filePath);
            String markdownContent = Files.readString(Paths.get(filePath));
            
            // 移除Markdown标记
            String text = removeMarkdownSyntax(markdownContent);
            
            log.info("Markdown文件解析完成, 长度: {}", text.length());
            return text;
        } catch (Exception e) {
            log.error("Markdown文件解析失败: {}", filePath, e);
            throw new RuntimeException("Markdown文件解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 移除Markdown语法标记
     */
    private String removeMarkdownSyntax(String markdown) {
        // 移除标题标记
        String text = markdown.replaceAll("^#+\\s+", "");
        // 移除粗体/斜体标记
        text = text.replaceAll("[*`_]", "");
        // 移除链接标记
        text = text.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        // 移除图片标记
        text = text.replaceAll("!\\[([^\\]]*)\\]\\([^)]+\\)", "$1");
        // 移除代码块标记
        text = text.replaceAll("```[\\s\\S]*?```", "");
        // 移除行内代码
        text = text.replaceAll("`([^`]+)`", "$1");
        
        return text.trim();
    }

    @Override
    public String getSupportedFileType() {
        return "md";
    }
}
