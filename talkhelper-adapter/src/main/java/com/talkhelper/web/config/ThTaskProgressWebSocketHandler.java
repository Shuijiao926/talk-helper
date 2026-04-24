package com.talkhelper.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talkhelper.task.dto.ThTaskProgressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class ThTaskProgressWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, List<WebSocketSession>> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.info("收到WebSocket消息: {}", payload);

            Map<String, String> request = objectMapper.readValue(payload, Map.class);
            String action = request.get("action");
            String taskId = request.get("taskId");

            if ("subscribe".equals(action) && taskId != null) {
                sessionMap.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>()).add(session);
                log.info("客户端订阅任务进度: taskId={}, sessionId={}", taskId, session.getId());

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

        sessionMap.values().forEach(sessions -> sessions.removeIf(s -> s.equals(session)));
        sessionMap.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public void pushProgress(String taskId, ThTaskProgressDTO progress) {
        List<WebSocketSession> sessions = sessionMap.get(taskId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("WebSocket会话不存在或已关闭: taskId={}", taskId);
            return;
        }

        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                sendToSession(session, progress);
            }
        }

        sessions.removeIf(session -> !session.isOpen());
    }

    private void sendToSession(WebSocketSession session, ThTaskProgressDTO progress) {
        try {
            String json = objectMapper.writeValueAsString(progress);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

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

                String taskId = channel.substring("task:progress:".length());

                ThTaskProgressDTO progress = objectMapper.readValue(body, ThTaskProgressDTO.class);
                webSocketHandler.pushProgress(taskId, progress);

            } catch (Exception e) {
                log.error("处理Redis消息失败", e);
            }
        }
    }
}
