package com.talkhelper.textpreprocess.pipeline.handler;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessContext;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.strategy.cleaner.ThAiStructuredCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThColloquialCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThGeneralCleanerStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文本清洗处理器
 * 链式执行多个清洗策略:
 * 1. 通用清洗(必选)
 * 2. AI结构化清洗(可选)
 * 3. 口语化清洗(可选)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThTextCleanHandler implements ThTextProcessHandler {

    private final ThGeneralCleanerStrategy generalCleanerStrategy;
    private final ThAiStructuredCleanerStrategy aiStructuredCleanerStrategy;
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

        String text = context.getExtractedText();
        
        // 1. 始终先执行通用清洗(基础清洗)
        log.debug("[{}] 执行通用清洗策略", getName());
        text = generalCleanerStrategy.clean(text);
        
//        // 2. 根据配置决定是否执行AI结构化清洗
//        Boolean aiStructured = context.getRequest() != null
//                ? context.getRequest().getAiStructured()
//                : false;
//
//        if (aiStructured) {
//            log.debug("[{}] 执行AI结构化清洗策略", getName());
//            text = aiStructuredCleanerStrategy.clean(text);
//        }
        
        // 3. 根据配置决定是否执行口语化清洗
        Boolean colloquialize = context.getRequest() != null 
                ? context.getRequest().getColloquialize() 
                : false;
        
        if (colloquialize) {
            log.debug("[{}] 执行口语化清洗策略", getName());
            text = colloquialCleanerStrategy.clean(text);
        }
        
        context.setCleanedText(text);
        
        log.info("[{}] 文本清洗完成, 原始长度: {}, 清洗后长度: {}", 
                getName(), context.getExtractedText().length(), text.length());
    }
}
