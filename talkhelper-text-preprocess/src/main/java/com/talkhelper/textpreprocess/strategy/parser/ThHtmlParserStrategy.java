package com.talkhelper.textpreprocess.strategy.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * HTML文件解析器
 * 使用Jsoup清理HTML标签,提取正文
 */
@Slf4j
@Component
public class ThHtmlParserStrategy implements ThDocumentParserStrategy {

    @Override
    public String parse(String filePath) {
        try {
            log.info("开始解析HTML文件: {}", filePath);
            String htmlContent = Files.readString(Paths.get(filePath));
            
            // 使用Jsoup解析HTML
            org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
            
            // 清理HTML标签,保留基本格式
            String cleanHtml = Jsoup.clean(doc.body().html(), Safelist.relaxed());
            
            // 再次解析为纯文本
            String text = Jsoup.parse(cleanHtml).text();
            
            log.info("HTML文件解析完成, 长度: {}", text.length());
            return text;
        } catch (Exception e) {
            log.error("HTML文件解析失败: {}", filePath, e);
            throw new RuntimeException("HTML文件解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedFileType() {
        return "html";
    }
}
