package com.talkhelper.textpreprocess.pipeline.config;

import com.roamingguide.starter.llm.LlmService;
import com.roamingguide.starter.storage.ObjectStorageFactory;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.pipeline.handler.*;
import com.talkhelper.textpreprocess.service.ThContentSaveService;
import com.talkhelper.textpreprocess.service.ThPodcastPromptTemplateService;
import com.talkhelper.textpreprocess.strategy.chunk.ThChapterChunkStrategy;
import com.talkhelper.textpreprocess.strategy.chunk.ThFixedChunkStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThAiStructuredCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThColloquialCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThGeneralCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.parser.ThDocumentParserFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThContentSaverFactory;
import com.talkhelper.textpreprocess.strategy.saver.ThMultiStrategySaverExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.ExecutorService;

/**
 * 管道处理器配置
 * 定义处理器的执行顺序
 */
@Configuration
@RequiredArgsConstructor
public class ThTextProcessPipelineConfig {

    private final ThDocumentParserFactory parserFactory;
    private final ThContentSaveService contentSaveService;
    private final ThGeneralCleanerStrategy generalCleanerStrategy;
    private final ThAiStructuredCleanerStrategy aiStructuredCleanerStrategy;
    private final ThColloquialCleanerStrategy colloquialCleanerStrategy;
    private final ThFixedChunkStrategy fixedChunkStrategy;
    private final ThChapterChunkStrategy chapterChunkStrategy;
    private final LlmService llmService;
    private final ThPodcastPromptTemplateService promptTemplateService;
    private final ExecutorService ioIntensiveExecutor;  // IO密集型线程池
    private final ObjectStorageFactory storageFactory;

    /**
     * 文档解析处理器 - 第1步
     */
    @Bean
    @Order(1)
    public ThDocumentParseHandler documentParseHandler() {
        return new ThDocumentParseHandler(parserFactory);
    }

    /**
     * 文本清洗处理器 - 第2步
     */
    @Bean
    @Order(2)
    public ThTextCleanHandler textCleanHandler() {
        return new ThTextCleanHandler(generalCleanerStrategy, aiStructuredCleanerStrategy, colloquialCleanerStrategy);
    }

    /**
     * 文本分块处理器 - 第3步
     */
    @Bean
    @Order(3)
    public ThTextChunkHandler textChunkHandler() {
        return new ThTextChunkHandler(fixedChunkStrategy, chapterChunkStrategy);
    }

    /**
     * AI处理处理器 - 第4步（可选）
     */
    @Bean
    @Order(4)
    public ThAiProcessHandler aiProcessHandler() {
        return new ThAiProcessHandler(llmService, promptTemplateService, ioIntensiveExecutor);
    }

    /**
     * 结果构建处理器 - 第5步
     */
    @Bean
    @Order(5)
    public ThResultBuildHandler resultBuildHandler() {
        return new ThResultBuildHandler(storageFactory);
    }
}
