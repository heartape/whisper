package com.heartape.whisper.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<Long, ConcurrentHashMap<WebSocketSession, Long>> sessions = new ConcurrentHashMap<>();

    public void register(Long userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(session, System.currentTimeMillis());
    }

    public void remove(Long userId, WebSocketSession session) {
        ConcurrentHashMap<WebSocketSession, Long> sessionMap = sessions.get(userId);
        if (sessionMap != null) {
            sessionMap.remove(session);
        }
    }

    public void send(Long userId, String msg) {
        ConcurrentHashMap<WebSocketSession, Long> sessionMap = sessions.get(userId);
        if (sessionMap != null) {
            sessionMap.forEach((s, l) -> {
                try {
                    s.sendMessage(new TextMessage(msg));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public void removeAll(Long userId) {
        removeAll(userId, null);
    }

    public void removeAll(Long userId, CloseStatus closeStatus) {
        sessions.computeIfPresent(userId, (id, sessionMap) -> {
            sessionMap.forEach((webSocketSession, l) -> {
                try {
                    if (webSocketSession != null && webSocketSession.isOpen()) {
                        webSocketSession.close(closeStatus);
                        sessionMap.remove(webSocketSession);
                    }
                } catch (Exception ignored) {}
            });
            return sessionMap;
        });
        sessions.remove(userId);
    }

}
