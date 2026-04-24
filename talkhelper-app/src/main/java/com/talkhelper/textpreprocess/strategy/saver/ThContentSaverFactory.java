package com.talkhelper.textpreprocess.strategy.saver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容保存策略工厂
 * 根据保存类型获取对应的保存策略
 */
@Slf4j
@Component
public class ThContentSaverFactory {

    private final Map<String, ThContentSaverStrategy> saverStrategyMap;

    public ThContentSaverFactory(List<ThContentSaverStrategy> savers) {
        // 将所有保存策略注册到Map中，key为策略名称
        this.saverStrategyMap = savers.stream()
                .collect(Collectors.toMap(
                        ThContentSaverStrategy::getStrategyName,
                        saver -> saver
                ));
        log.info("内容保存策略工厂初始化完成, 支持的策略: {}", saverStrategyMap.keySet());
    }

    /**
     * 根据保存类型获取保存策略
     *
     * @param strategyType 保存类型（local-file, database, vector-db）
     * @return 对应的保存策略
     */
    public ThContentSaverStrategy getSaver(String strategyType) {
        if (strategyType == null || strategyType.trim().isEmpty()) {
            log.warn("未指定保存策略，使用默认策略: local-file");
            strategyType = "local-file";
        }
        
        ThContentSaverStrategy saver = saverStrategyMap.get(strategyType.toLowerCase());
        if (saver == null) {
            log.error("不支持的保存策略: {}, 支持的策略: {}", strategyType, saverStrategyMap.keySet());
            throw new UnsupportedOperationException("不支持的保存策略: " + strategyType + ", 支持的策略: " + saverStrategyMap.keySet());
        }
        
        log.debug("使用保存策略: {}", strategyType);
        return saver;
    }

    /**
     * 判断是否支持该保存策略
     *
     * @param strategyType 保存类型
     * @return 是否支持
     */
    public boolean isSupported(String strategyType) {
        return saverStrategyMap.containsKey(strategyType.toLowerCase());
    }

    /**
     * 获取所有支持的保存策略
     *
     * @return 支持的策略列表
     */
    public List<String> getSupportedStrategies() {
        return List.copyOf(saverStrategyMap.keySet());
    }
}
