package com.talkhelper.textpreprocess.pipeline.config;

import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.pipeline.handler.*;
import com.talkhelper.textpreprocess.strategy.chunk.ThChapterChunkStrategy;
import com.talkhelper.textpreprocess.strategy.chunk.ThFixedChunkStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThColloquialCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThGeneralCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.parser.ThDocumentParserFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThContentSaverFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThMultiStrategySaverExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 管道处理器配置
 * 定义处理器的执行顺序
 */
@Configuration
@RequiredArgsConstructor
public class ThTextProcessPipelineConfig {

    private final ThDocumentParserFactory parserFactory;
    private final ThContentSaverFactory saverFactory;
    private final ThMultiStrategySaverExecutor multiStrategyExecutor;
    private final ThGeneralCleanerStrategy generalCleanerStrategy;
    private final ThColloquialCleanerStrategy colloquialCleanerStrategy;
    private final ThFixedChunkStrategy fixedChunkStrategy;
    private final ThChapterChunkStrategy chapterChunkStrategy;

    /**
     * 文件保存处理器 - 第1步
     */
    @Bean
    @Order(1)
    public ThFileSaveHandler fileSaveHandler() {
        return new ThFileSaveHandler(saverFactory, multiStrategyExecutor);
    }

    /**
     * 文档解析处理器 - 第2步
     */
    @Bean
    @Order(2)
    public ThDocumentParseHandler documentParseHandler() {
        return new ThDocumentParseHandler(parserFactory);
    }

    /**
     * 文本清洗处理器 - 第3步
     */
    @Bean
    @Order(3)
    public ThTextCleanHandler textCleanHandler() {
        return new ThTextCleanHandler(generalCleanerStrategy, colloquialCleanerStrategy);
    }

    /**
     * 文本分块处理器 - 第4步
     */
    @Bean
    @Order(4)
    public ThTextChunkHandler textChunkHandler() {
        return new ThTextChunkHandler(fixedChunkStrategy, chapterChunkStrategy);
    }

    /**
     * AI处理处理器 - 第5步（可选）
     */
    @Bean
    @Order(5)
    public ThAiProcessHandler aiProcessHandler() {
        return new ThAiProcessHandler();
    }

    /**
     * 结果构建处理器 - 第6步
     */
    @Bean
    @Order(6)
    public ThResultBuildHandler resultBuildHandler() {
        return new ThResultBuildHandler(saverFactory, multiStrategyExecutor);
    }
}
