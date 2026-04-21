package com.roamingguide.starter.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象存储策略工厂
 */
@Slf4j
@RequiredArgsConstructor
public class ObjectStorageFactory {

    private final List<ObjectStorageStrategy> strategies;
    private final StorageProperties storageProperties;
    private final Map<String, ObjectStorageStrategy> strategyMap = new ConcurrentHashMap<>();

    public ObjectStorageStrategy getActiveStorage() {
        String activeType = storageProperties.getActiveType();

        ObjectStorageStrategy strategy = strategyMap.get(activeType);
        if (strategy == null) {
            for (ObjectStorageStrategy s : strategies) {
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

    private ObjectStorageStrategy findAvailableStrategy() {
        for (ObjectStorageStrategy strategy : strategies) {
            if (strategy.isAvailable()) {
                log.info("使用备用存储策略: {}", strategy.getType());
                return strategy;
            }
        }
        throw new IllegalStateException("没有可用的对象存储策略");
    }
}
