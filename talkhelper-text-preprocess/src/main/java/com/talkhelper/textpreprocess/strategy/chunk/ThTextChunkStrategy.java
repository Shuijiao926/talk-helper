package com.talkhelper.textpreprocess.strategy.chunk;

import com.talkhelper.textpreprocess.vo.ThTextChunkVO;

import java.util.List;

/**
 * 文本分块策略接口
 * 使用策略模式,支持不同的分块方式
 */
public interface ThTextChunkStrategy {

    /**
     * 将文本分块
     *
     * @param text   待分块的文本
     * @param config 分块配置
     * @return 分块结果列表
     */
    List<ThTextChunkVO> chunk(String text, Object config);

    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getStrategyName();
}
