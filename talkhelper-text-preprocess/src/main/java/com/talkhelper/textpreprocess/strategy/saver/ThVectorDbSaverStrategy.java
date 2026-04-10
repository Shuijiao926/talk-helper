package com.talkhelper.textpreprocess.strategy.saver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 向量数据库保存策略
 * 将文本分块后向量化，保存到向量数据库（如Milvus、Chroma等）
 * TODO: 待实现具体的向量数据库保存逻辑
 */
@Slf4j
@Component
public class ThVectorDbSaverStrategy implements ThContentSaverStrategy {

    @Override
    public String save(String content, Object metadata) {
        log.info("开始保存到向量数据库");
        
        // TODO: 实现向量数据库保存逻辑
        // 1. 文本分块（如果还未分块）
        // 2. 调用AI服务生成向量嵌入
        // 3. 保存到向量数据库
        // 4. 返回文档ID
        
        log.warn("向量数据库保存策略尚未实现，返回模拟ID");
        return "vector-doc-" + System.currentTimeMillis();
    }

    @Override
    public String getStrategyName() {
        return "vector-db";
    }
}
