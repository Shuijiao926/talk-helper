package com.talkhelper.common.storage;

import com.talkhelper.common.config.ThStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThObjectStorageFactory {

    private final List<ThObjectStorageStrategy> strategies;
    private final ThStorageConfig storageConfig;
    private final Map<String, ThObjectStorageStrategy> strategyMap = new ConcurrentHashMap<>();

    public ThObjectStorageStrategy getActiveStorage() {
        String activeType = storageConfig.getActiveType();

        ThObjectStorageStrategy strategy = strategyMap.get(activeType);
        if (strategy == null) {
            for (ThObjectStorageStrategy s : strategies) {
                strategyMap.put(s.getType(), s);
            }
            strategy = strategyMap.get(activeType);
        }

        if (strategy == null) {
            throw new IllegalStateException("未找到存储策略: " + activeType);
        }

        if (!strategy.isAvailable()) {
            log.warn("存储策略不可用: {}, 尝试使用其他可用策略", activeType);
            return findAvailableStrategy();
        }

        return strategy;
    }

    private ThObjectStorageStrategy findAvailableStrategy() {
        for (ThObjectStorageStrategy strategy : strategies) {
            if (strategy.isAvailable()) {
                log.info("使用备用存储策略: {}", strategy.getType());
                return strategy;
            }
        }
        throw new IllegalStateException("没有可用的对象存储策略");
    }
}
