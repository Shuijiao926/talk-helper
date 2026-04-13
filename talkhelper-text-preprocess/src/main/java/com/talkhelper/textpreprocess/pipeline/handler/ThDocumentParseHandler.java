package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.service.ThContentSaveService;
import com.talkhelper.textpreprocess.strategy.parser.ThDocumentParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文档解析处理器
 * 负责将文件解析为纯文本
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThDocumentParseHandler implements ThTextProcessHandler {

    private final ThDocumentParserFactory parserFactory;
    private final ThContentSaveService contentSaveService;

    @Override
    public String getName() {
        return "文档解析处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        return (context.getFile() != null || context.getFilePath() != null) && context.getExtractedText() == null;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始解析文档", getName());

        // 使用工厂获取对应的解析器
        var parser = parserFactory.getParser(context.getFileType());
        String extractedText;
        
        // 优先从 MultipartFile 直接解析（避免不必要的文件保存）
        if (context.getFile() != null) {
            log.info("[{}] 从 MultipartFile 直接解析: {}", getName(), context.getFile().getOriginalFilename());
            try {
                extractedText = parser.parse(
                    context.getFile().getInputStream(), 
                    context.getFile().getOriginalFilename()
                );
            } catch (UnsupportedOperationException e) {
                // 如果不支持流式解析，则临时保存到本地后解析
                log.warn("[{}] 解析器不支持流式解析，临时保存文件: {}", getName(), e.getMessage());
                Path tempFile = saveToTempFile(context.getFile());
                try {
                    extractedText = parser.parse(tempFile.toString());
                } finally {
                    // 解析完成后删除临时文件
                    Files.deleteIfExists(tempFile);
                    log.debug("[{}] 临时文件已删除: {}", getName(), tempFile);
                }
            }
        } else if (context.getFilePath() != null) {
            // 从文件路径解析
            log.info("[{}] 从文件路径解析: {}", getName(), context.getFilePath());
            extractedText = parser.parse(context.getFilePath());
        } else {
            throw new IllegalStateException("没有可用的文件输入");
        }
        
        context.setExtractedText(extractedText);
        
        log.info("[{}] 文档解析完成, 文本长度: {}", getName(), extractedText.length());
    }

    /**
     * 将MultipartFile保存到临时文件
     */
    private Path saveToTempFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf('.')) 
                : ".tmp";
        
        Path tempFile = Files.createTempFile("talkhelper_", suffix);
        file.transferTo(tempFile.toFile());
        
        log.debug("[{}] 临时文件创建成功: {}, 大小: {} bytes", 
                getName(), tempFile, file.getSize());
        
        return tempFile;
    }
}
