package com.roamingguide.starter.llm;

import com.dtflys.forest.springboot.annotation.ForestScan;
import com.roamingguide.starter.llm.dashscope.DashScopeApi;
import com.roamingguide.starter.llm.dashscope.DashScopeLlmClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * LLM Starter 自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(LlmProperties.class)
@ForestScan(basePackages = "com.roamingguide.starter.llm")
public class LlmAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "spring.ai.dashscope.api-key")
    public DashScopeLlmClient dashScopeLlmClient(DashScopeApi dashScopeApi) {
        return new DashScopeLlmClient(dashScopeApi);
    }

    @Bean
    @ConditionalOnMissingBean
    public LlmClientFactory llmClientFactory(List<LlmClient> llmClients, LlmProperties properties) {
        return new LlmClientFactory(llmClients, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LlmService llmService(LlmClientFactory llmClientFactory) {
        return new LlmService(llmClientFactory);
    }
}
