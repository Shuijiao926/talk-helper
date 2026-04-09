package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.strategy.parser.ThDocumentParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文档解析处理器
 * 负责将文件解析为纯文本
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThDocumentParseHandler implements ThTextProcessHandler {

    private final ThDocumentParserFactory parserFactory;

    @Override
    public String getName() {
        return "文档解析处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        // 如果有文件路径且还没有提取文本，则需要解析
        return context.getFilePath() != null && context.getExtractedText() == null;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始解析文档: {}", getName(), context.getFilePath());

        // 使用工厂获取对应的解析器并解析
        String extractedText = parserFactory.parse(context.getFilePath(), context.getFileType());
        
        context.setExtractedText(extractedText);
        
        log.info("[{}] 文档解析完成, 文本长度: {}", getName(), extractedText.length());
    }
}
