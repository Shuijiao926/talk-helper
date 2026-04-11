package com.talkhelper.common.llm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM客户端工厂（工厂模式）
 * 根据配置动态选择LLM提供商
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThLlmClientFactory {

    private final List<ThLlmClient> llmClients;

    @Value("${talkhelper.llm.provider:dashscope}")
    private String llmProvider;

    private ThLlmClient activeClient;
    private Map<String, ThLlmClient> clientMap;

    @PostConstruct
    public void init() {
        // 构建客户端映射表
        clientMap = llmClients.stream()
                .collect(Collectors.toMap(ThLlmClient::getType, client -> client));

        // 根据配置选择客户端
        activeClient = clientMap.get(llmProvider.toLowerCase());

        if (activeClient == null) {
            log.warn("配置的LLM提供商 [{}] 不存在，使用默认DashScope", llmProvider);
            activeClient = clientMap.get("dashscope");
        }

        log.info("========== LLM客户端初始化完成 ==========");
        log.info("可用LLM提供商: {}", clientMap.keySet());
        log.info("当前激活LLM: {} ({})", activeClient.getType(), activeClient.getClass().getSimpleName());
    }

    /**
     * 获取当前激活的LLM客户端
     */
    public ThLlmClient getActiveClient() {
        return activeClient;
    }

    /**
     * 根据类型获取LLM客户端
     */
    public ThLlmClient getClient(String type) {
        ThLlmClient client = clientMap.get(type.toLowerCase());
        if (client == null) {
            throw new IllegalArgumentException("不支持的LLM提供商: " + type);
        }
        return client;
    }
}
