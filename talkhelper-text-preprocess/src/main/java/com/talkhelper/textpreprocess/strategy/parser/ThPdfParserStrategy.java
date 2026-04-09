package com.talkhelper.textpreprocess.strategy.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * PDF文件解析器
 * 使用Apache PDFBox提取文本
 */
@Slf4j
@Component
public class ThPdfParserStrategy implements ThDocumentParserStrategy {

    @Override
    public String parse(String filePath) {
        PDDocument document = null;
        try {
            log.info("开始解析PDF文件: {}", filePath);
            document = PDDocument.load(new File(filePath));
            
            // 检查是否加密
            if (document.isEncrypted()) {
                throw new RuntimeException("PDF文件已加密,无法解析");
            }
            
            // 提取文本
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            log.info("PDF文件解析完成, 页数: {}, 文本长度: {}", 
                    document.getNumberOfPages(), text.length());
            return text;
        } catch (Exception e) {
            log.error("PDF文件解析失败: {}", filePath, e);
            throw new RuntimeException("PDF文件解析失败: " + e.getMessage(), e);
        } finally {
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    log.error("关闭PDF文档失败", e);
                }
            }
        }
    }

    @Override
    public String getSupportedFileType() {
        return "pdf";
    }
}
