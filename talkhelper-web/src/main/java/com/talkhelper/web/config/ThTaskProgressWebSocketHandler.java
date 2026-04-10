package com.talkhelper.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.task.dto.ThTaskProgressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务进度WebSocket Handler
 */
@Slf4j
public class ThTaskProgressWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储taskId -> WebSocketSession的映射
    private static final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.info("收到WebSocket消息: {}", payload);

            // 客户端发送订阅请求：{"action": "subscribe", "taskId": "xxx"}
            Map<String, String> request = objectMapper.readValue(payload, Map.class);
            String action = request.get("action");
            String taskId = request.get("taskId");

            if ("subscribe".equals(action) && taskId != null) {
                sessionMap.put(taskId, session);
                log.info("客户端订阅任务进度: taskId={}, sessionId={}", taskId, session.getId());
                
                // 发送确认消息
                sendToSession(session, ThTaskProgressDTO.builder()
                        .taskId(taskId)
                        .message("已订阅任务进度")
                        .build());
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket连接关闭: {}, status: {}", session.getId(), status);
        
        // 清理session映射
        sessionMap.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    /**
     * 推送任务进度到指定任务的所有订阅者
     */
    public void pushProgress(String taskId, ThTaskProgressDTO progress) {
        WebSocketSession session = sessionMap.get(taskId);
        if (session != null && session.isOpen()) {
            sendToSession(session, progress);
        } else {
            log.debug("WebSocket会话不存在或已关闭: taskId={}", taskId);
        }
    }

    /**
     * 发送消息到会话
     */
    private void sendToSession(WebSocketSession session, ThTaskProgressDTO progress) {
        try {
            String json = objectMapper.writeValueAsString(progress);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    /**
     * Redis消息监听器 - 订阅任务进度频道
     */
    public static class TaskProgressRedisListener implements MessageListener {

        private final ThTaskProgressWebSocketHandler webSocketHandler;
        private final ObjectMapper objectMapper = new ObjectMapper();

        public TaskProgressRedisListener(ThTaskProgressWebSocketHandler webSocketHandler) {
            this.webSocketHandler = webSocketHandler;
        }

        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                String channel = new String(message.getChannel());
                String body = new String(message.getBody());
                
                // 从channel中提取taskId: task:progress:{taskId}
                String taskId = channel.substring("task:progress:".length());
                
                ThTaskProgressDTO progress = objectMapper.readValue(body, ThTaskProgressDTO.class);
                webSocketHandler.pushProgress(taskId, progress);
                
            } catch (Exception e) {
                log.error("处理Redis消息失败", e);
            }
        }
    }
}
