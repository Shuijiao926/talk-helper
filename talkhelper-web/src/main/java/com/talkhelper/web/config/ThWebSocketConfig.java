package com.talkhelper.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置（条件化）
 * 仅在Redis可用时启用（因为需要Redis Pub/Sub推送进度）
 */
@Slf4j
@Configuration
@EnableWebSocket
@ConditionalOnBean(RedisTemplate.class) // 只有Redis可用时才启用WebSocket
public class ThWebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 允许跨域
        registry.addHandler(taskProgressWebSocketHandler(), "/ws/task-progress")
                .setAllowedOrigins("*");
        log.info("✅ WebSocket已启用: /ws/task-progress");
    }

    @Bean
    public ThTaskProgressWebSocketHandler taskProgressWebSocketHandler() {
        return new ThTaskProgressWebSocketHandler();
    }
}
