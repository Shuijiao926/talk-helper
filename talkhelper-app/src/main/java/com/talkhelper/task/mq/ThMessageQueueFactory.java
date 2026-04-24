package com.talkhelper.task.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息队列工厂（工厂模式）
 * 根据配置动态选择MQ实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ThMessageQueueFactory {

    private final List<ThMessageQueue> messageQueues;
    
    @Value("${talkhelper.mq.type:redis}")
    private String mqType;
    
    private ThMessageQueue activeMQ;
    
    private Map<String, ThMessageQueue> mqMap;

    @PostConstruct
    public void init() {
        // 构建MQ映射表
        mqMap = messageQueues.stream()
                .collect(Collectors.toMap(ThMessageQueue::getType, mq -> mq));
        
        // 根据配置选择MQ
        activeMQ = mqMap.get(mqType.toLowerCase());
        
        if (activeMQ == null) {
            log.warn("配置的MQ类型 [{}] 不存在，使用默认Redis MQ", mqType);
            activeMQ = mqMap.get("redis");
        }
        
        log.info("========== 消息队列初始化完成 ==========");
        log.info("可用MQ类型: {}", mqMap.keySet());
        log.info("当前激活MQ: {} ({})", activeMQ.getType(), activeMQ.getClass().getSimpleName());
    }

    /**
     * 获取当前激活的MQ实例
     */
    public ThMessageQueue getActiveMQ() {
        return activeMQ;
    }

    /**
     * 根据类型获取MQ实例
     */
    public ThMessageQueue getMQ(String type) {
        ThMessageQueue mq = mqMap.get(type.toLowerCase());
        if (mq == null) {
            throw new IllegalArgumentException("不支持的MQ类型: " + type);
        }
        return mq;
    }
}
