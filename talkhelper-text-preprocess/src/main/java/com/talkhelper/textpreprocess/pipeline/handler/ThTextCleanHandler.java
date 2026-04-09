package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.strategy.cleaner.ThColloquialCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThGeneralCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThTextCleanerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文本清洗处理器
 * 根据配置选择口语化清洗或通用清洗
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThTextCleanHandler implements ThTextProcessHandler {

    private final ThGeneralCleanerStrategy generalCleanerStrategy;
    private final ThColloquialCleanerStrategy colloquialCleanerStrategy;

    @Override
    public String getName() {
        return "文本清洗处理器";
    }

    @Override
    public boolean shouldHandle(ThTextProcessContext context) {
        // 如果有提取的文本且还没有清洗，则需要清洗
        return context.getExtractedText() != null && context.getCleanedText() == null;
    }

    @Override
    public void handle(ThTextProcessContext context) throws Exception {
        log.info("[{}] 开始清洗文本", getName());

        // 根据配置选择清洗策略
        Boolean colloquialize = context.getRequest() != null 
                ? context.getRequest().getColloquialize() 
                : false;
        
        ThTextCleanerStrategy strategy = colloquialize 
                ? colloquialCleanerStrategy 
                : generalCleanerStrategy;

        String cleanedText = strategy.clean(context.getExtractedText());
        
        context.setCleanedText(cleanedText);
        
        log.info("[{}] 文本清洗完成, 原始长度: {}, 清洗后长度: {}", 
                getName(), context.getExtractedText().length(), cleanedText.length());
    }
}
