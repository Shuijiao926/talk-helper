package com.talkhelper.textpreprocess.strategy.saver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 关系型数据库保存策略
 * 将文本内容保存到MySQL等关系型数据库
 * TODO: 待实现具体的数据库保存逻辑
 */
@Slf4j
@Component
public class ThDatabaseSaverStrategy implements ThContentSaverStrategy {

    @Override
    public String save(String content, Object metadata) {
        log.info("开始保存到关系型数据库");
        
        // TODO: 实现数据库保存逻辑
        // 1. 构建实体对象
        // 2. 调用Mapper/Repository保存
        // 3. 返回记录ID
        
        log.warn("数据库保存策略尚未实现，返回模拟ID");
        return "db-record-" + System.currentTimeMillis();
    }

    @Override
    public String getStrategyName() {
        return "database";
    }
}
