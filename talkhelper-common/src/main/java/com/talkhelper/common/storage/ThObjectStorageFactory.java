package com.talkhelper.common.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象存储工厂
 * 根据配置动态选择存储策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThObjectStorageFactory {

    private final List<ThObjectStorageStrategy> strategies;
    private final Map<String, ThObjectStorageStrategy> strategyMap = new ConcurrentHashMap<>();

    /**
     * 获取激活的存储策略
     */
    public ThObjectStorageStrategy getActiveStorage() {
        // TODO: 从配置中读取激活的存储类型,默认使用MinIO
        String activeType = "minio";
        
        ThObjectStorageStrategy strategy = strategyMap.get(activeType);
        if (strategy == null) {
            // 初始化策略映射
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

    /**
     * 查找第一个可用的存储策略
     */
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
