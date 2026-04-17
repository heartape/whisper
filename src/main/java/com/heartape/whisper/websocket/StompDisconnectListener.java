package com.heartape.whisper.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
@Component
public class StompDisconnectListener {

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();

        Long userId = user != null ? Long.valueOf(user.getName()) : null;

        // 👇 这里就是「心跳失败 / 客户端断线」
        log.warn("STOMP disconnect: sessionId={}, userId={}", sessionId, userId);

        // TODO:
        // 1. 标记用户离线
        // 2. 清理连接映射
        // 3. IM 在线状态处理
    }
}

