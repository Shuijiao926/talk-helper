package com.talkhelper.textpreprocess.strategy.saver;

/**
 * 内容保存策略接口
 * 支持多种保存方式：本地文件、关系型数据库、向量数据库等
 */
public interface ThContentSaverStrategy {

    /**
     * 保存内容
     *
     * @param content 要保存的内容（文本或文件路径）
     * @param metadata 元数据（可选）
     * @return 保存后的标识（文件路径、记录ID等）
     */
    String save(String content, Object metadata);

    /**
     * 获取保存策略名称
     *
     * @return 策略名称（local-file, database, vector-db等）
     */
    String getStrategyName();

    /**
     * 判断是否支持该保存类型
     *
     * @param strategyType 保存类型
     * @return 是否支持
     */
    default boolean supports(String strategyType) {
        return getStrategyName().equalsIgnoreCase(strategyType);
    }
}
