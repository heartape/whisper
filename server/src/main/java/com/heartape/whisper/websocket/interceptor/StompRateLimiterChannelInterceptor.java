package com.heartape.whisper.websocket.interceptor;

import com.heartape.whisper.websocket.WebsocketStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
public class StompRateLimiterChannelInterceptor implements ChannelInterceptor {

    private final RateLimiterBucketManager userBucketManager = new RateLimiterBucketManager(50, 10, 1);
    private final RateLimiterBucketManager ipBucketManager = new RateLimiterBucketManager(50, 10, 1);
    private final RateLimiterBucketManager connectBucketManager = new RateLimiterBucketManager(50, 10, 1);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();
        Principal user = accessor.getUser();
        String ipAddress = getClientIp(accessor);

        if (!connectBucketManager.consume(sessionId, 1)){
            log.warn("连接速率限制 - sessionId: {}", sessionId);
            throw new MessageHandlingException(message, WebsocketStatus.CONNECT_RATE_LIMIT);
        }
        if (!ipBucketManager.consume(ipAddress, 1)){
            log.warn("IP速率限制 - IP: {}", ipAddress);
            throw new MessageHandlingException(message, WebsocketStatus.IP_RATE_LIMIT);
        }
        if (user != null) {
            String userId = user.getName();
            if (!userBucketManager.consume(userId, 1)){
                log.warn("账户速率限制 - userId: {}", userId);
                throw new MessageHandlingException(message, WebsocketStatus.USER_RATE_LIMIT);
            }
        }
        return message;
    }

    private String getClientIp(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object ip = sessionAttributes.get("ip");
            if (ip instanceof String) {
                return (String) ip;
            }
        }
        return "unknown";
    }

}
