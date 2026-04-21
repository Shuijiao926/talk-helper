package com.roamingguide.starter.llm;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM 客户端工厂（工厂模式）
 * 根据配置动态选择 LLM 提供商
 */
@Slf4j
@RequiredArgsConstructor
public class LlmClientFactory {

    private final List<LlmClient> llmClients;
    private final LlmProperties properties;

    private LlmClient activeClient;
    private Map<String, LlmClient> clientMap;

    @PostConstruct
    public void init() {
        clientMap = llmClients.stream()
                .collect(Collectors.toMap(LlmClient::getType, client -> client));

        activeClient = clientMap.get(properties.getProvider().toLowerCase());

        if (activeClient == null) {
            log.warn("配置的LLM提供商 [{}] 不存在，使用默认DashScope", properties.getProvider());
            activeClient = clientMap.get("dashscope");
        }

        log.info("========== LLM客户端初始化完成 ==========");
        log.info("可用LLM提供商: {}", clientMap.keySet());
        log.info("当前激活LLM: {} ({})", activeClient.getType(), activeClient.getClass().getSimpleName());
    }

    public LlmClient getActiveClient() {
        return activeClient;
    }

    public LlmClient getClient(String type) {
        LlmClient client = clientMap.get(type.toLowerCase());
        if (client == null) {
            throw new IllegalArgumentException("不支持的LLM提供商: " + type);
        }
        return client;
    }
}
