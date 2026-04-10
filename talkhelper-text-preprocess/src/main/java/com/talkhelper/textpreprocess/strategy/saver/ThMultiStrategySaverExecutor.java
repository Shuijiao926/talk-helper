package com.talkhelper.textpreprocess.strategy.saver;

import com.talkhelper.common.enums.ThContentSaveStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * 多策略保存执行器
 * 使用函数式编程思想，统一处理多策略保存逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThMultiStrategySaverExecutor {

    private final ThContentSaverFactory saverFactory;

    /**
     * 执行多策略保存
     *
     * @param strategies 保存策略数组
     * @param saveAction 保存动作（函数式接口）
     * @param actionName 动作名称（用于日志）
     */
    public void execute(ThContentSaveStrategy[] strategies, Consumer<ThContentSaverStrategy> saveAction, String actionName) {
        if (strategies == null || strategies.length == 0) {
            log.warn("未配置保存策略，跳过执行: {}", actionName);
            return;
        }

        log.info("开始执行 [{}], 共 {} 个策略", actionName, strategies.length);

        Arrays.stream(strategies)
                .forEach(strategy -> executeSingleStrategy(strategy, saveAction, actionName));
        
        log.info("[{}] 执行完成", actionName);
    }

    /**
     * 执行单个策略
     */
    private void executeSingleStrategy(ThContentSaveStrategy strategy, 
                                      Consumer<ThContentSaverStrategy> saveAction, 
                                      String actionName) {
        try {
            log.debug("执行策略: [{}]", strategy.getDescription());
            saveAction.accept(saverFactory.getSaver(strategy.getCode()));
            log.info("策略 [{}] 执行成功: {}", strategy.getDescription(), actionName);
        } catch (Exception e) {
            log.error("策略 [{}] 执行失败: {}", strategy.getDescription(), actionName, e);
            // 继续执行其他策略，不中断整个流程
        }
    }

    /**
     * 执行多策略保存（带过滤）
     *
     * @param strategies 保存策略数组
     * @param saveAction 保存动作
     * @param actionName 动作名称
     * @param filterStrategies 需要过滤的策略（如已在前置步骤处理过的策略）
     */
    public void executeWithFilter(ThContentSaveStrategy[] strategies,
                                  Consumer<ThContentSaverStrategy> saveAction,
                                  String actionName,
                                  ThContentSaveStrategy... filterStrategies) {
        if (strategies == null || strategies.length == 0) {
            return;
        }

        Arrays.stream(strategies)
                .filter(strategy -> !Arrays.asList(filterStrategies).contains(strategy))
                .forEach(strategy -> executeSingleStrategy(strategy, saveAction, actionName));
    }
}
