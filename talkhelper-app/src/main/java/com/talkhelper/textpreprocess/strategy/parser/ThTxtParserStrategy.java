package com.talkhelper.textpreprocess.strategy.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * TXT文件解析器
 */
@Slf4j
@Component
public class ThTxtParserStrategy implements ThDocumentParserStrategy {

    @Override
    public String parse(String filePath) {
        try {
            log.info("开始解析TXT文件: {}", filePath);
            String content = Files.readString(Paths.get(filePath));
            log.info("TXT文件解析完成, 长度: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("TXT文件解析失败: {}", filePath, e);
            throw new RuntimeException("TXT文件解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String parse(InputStream inputStream, String fileName) {
        try {
            log.info("开始从输入流解析TXT文件: {}", fileName);
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("TXT文件解析完成, 长度: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("TXT文件解析失败: {}", fileName, e);
            throw new RuntimeException("TXT文件解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedFileType() {
        return "txt";
    }
}
