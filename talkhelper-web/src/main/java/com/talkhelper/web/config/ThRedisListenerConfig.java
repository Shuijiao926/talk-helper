package com.talkhelper.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis消息监听配置（条件化）
 * 仅在Redis可用时创建相关Bean
 */
@Slf4j
@Configuration
@ConditionalOnBean(RedisTemplate.class) // 只有RedisTemplate存在时才加载
public class ThRedisListenerConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            ThTaskProgressWebSocketHandler.TaskProgressRedisListener listener) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        
        // 订阅任务进度频道
        container.addMessageListener(listener, new PatternTopic("task:progress:*"));
        
        log.info("✅ Redis消息监听器已启动");
        return container;
    }
    
    /**
     * 注册Redis消息监听器
     */
    @Bean
    public ThTaskProgressWebSocketHandler.TaskProgressRedisListener taskProgressRedisListener(
            ThTaskProgressWebSocketHandler webSocketHandler) {
        return new ThTaskProgressWebSocketHandler.TaskProgressRedisListener(webSocketHandler);
    }
}
