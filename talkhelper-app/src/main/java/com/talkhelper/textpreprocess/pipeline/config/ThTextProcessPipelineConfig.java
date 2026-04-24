package com.talkhelper.textpreprocess.pipeline.config;

import com.roamingguide.starter.llm.LlmService;
import com.talkhelper.common.domain.storage.ThObjectStorageGateway;
import com.talkhelper.textpreprocess.pipeline.ThTextProcessHandler;
import com.talkhelper.textpreprocess.pipeline.handler.ThAiProcessHandler;
import com.talkhelper.textpreprocess.pipeline.handler.ThDocumentParseHandler;
import com.talkhelper.textpreprocess.pipeline.handler.ThResultBuildHandler;
import com.talkhelper.textpreprocess.pipeline.handler.ThTextChunkHandler;
import com.talkhelper.textpreprocess.pipeline.handler.ThTextCleanHandler;
import com.talkhelper.textpreprocess.service.ThContentSaveService;
import com.talkhelper.textpreprocess.service.ThPodcastPromptTemplateService;
import com.talkhelper.textpreprocess.strategy.chunk.ThChapterChunkStrategy;
import com.talkhelper.textpreprocess.strategy.chunk.ThFixedChunkStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThAiStructuredCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThColloquialCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.cleaner.ThGeneralCleanerStrategy;
import com.talkhelper.textpreprocess.strategy.parser.ThDocumentParserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.concurrent.ExecutorService;

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
    private final ExecutorService ioIntensiveExecutor;
    private final ThObjectStorageGateway storageGateway;

    @Bean
    @Order(1)
    public ThDocumentParseHandler documentParseHandler() {
        return new ThDocumentParseHandler(parserFactory);
    }

    @Bean
    @Order(2)
    public ThTextCleanHandler textCleanHandler() {
        return new ThTextCleanHandler(generalCleanerStrategy, aiStructuredCleanerStrategy, colloquialCleanerStrategy);
    }

    @Bean
    @Order(3)
    public ThTextChunkHandler textChunkHandler() {
        return new ThTextChunkHandler(fixedChunkStrategy, chapterChunkStrategy);
    }

    @Bean
    @Order(4)
    public ThAiProcessHandler aiProcessHandler() {
        return new ThAiProcessHandler(llmService, promptTemplateService, ioIntensiveExecutor);
    }

    @Bean
    @Order(5)
    public ThResultBuildHandler resultBuildHandler() {
        return new ThResultBuildHandler(storageGateway);
    }
}
