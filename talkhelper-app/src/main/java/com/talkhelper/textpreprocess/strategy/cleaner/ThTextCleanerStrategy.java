package com.talkhelper.textpreprocess.strategy.cleaner;

/**
 * 文本清洗策略接口
 * 使用策略模式,支持不同的文本清洗方式
 */
public interface ThTextCleanerStrategy {

    /**
     * 清洗文本
     *
     * @param text 原始文本
     * @return 清洗后的文本
     */
    String clean(String text);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
}
