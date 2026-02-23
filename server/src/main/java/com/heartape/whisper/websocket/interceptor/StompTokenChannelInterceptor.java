package com.heartape.whisper.websocket.interceptor;

import com.heartape.whisper.common.JwtUtils;
import com.heartape.whisper.common.TokenUtils;
import com.heartape.whisper.entity.StompUser;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.service.UserService;
import com.heartape.whisper.websocket.WebSocketSessionManager;
import com.heartape.whisper.websocket.WebsocketStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;

import java.security.Principal;

@Slf4j
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@Configuration
public class StompTokenChannelInterceptor implements ChannelInterceptor {

    private final UserService userService;

    private final StringRedisTemplate redisTemplate;

    private final WebSocketSessionManager webSocketSessionManager;

    @SuppressWarnings("NullableProblems")
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            String bearToken = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (!TokenUtils.isBearerToken(bearToken)){
                throw new MessageDeliveryException(WebsocketStatus.INVALID_TOKEN);
            }
            String token = TokenUtils.getTokenFromBearerToken(bearToken);
            Claims claims;
            try {
                claims = JwtUtils.parse(token);
            } catch (ExpiredJwtException e) {
                throw new MessageDeliveryException(WebsocketStatus.EXPIRED_TOKEN);
            } catch (UnsupportedJwtException | IllegalArgumentException | SignatureException | MalformedJwtException e) {
                throw new MessageDeliveryException(WebsocketStatus.INVALID_TOKEN);
            }

            String subject = claims.getSubject();

            // 检查redis
            String token1 = redisTemplate.opsForValue().get(TokenUtils.createTokenStoreKey(subject));
            if (!StringUtils.hasText(token1)) {
                webSocketSessionManager.removeAll(Long.valueOf(subject), CloseStatus.NOT_ACCEPTABLE.withReason(WebsocketStatus.USER_NOT_FOUND));
                throw new MessageDeliveryException(WebsocketStatus.USER_NOT_FOUND);

            }

            User user = userService.getById(Long.valueOf(subject));
            if (user == null){
                throw new MessageDeliveryException(WebsocketStatus.USER_NOT_FOUND);
            }
            StompUser stompUser = new StompUser(subject, user.getUsername());
            accessor.setUser(stompUser);
        } else if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            // 鉴权，比如token过期和封号
            Principal user = accessor.getUser();
            if (user == null) {
                // 如果没有 Principal，说明未通过 CONNECT 鉴权或认证过期
                throw new MessageDeliveryException(WebsocketStatus.UNAUTHENTICATED_SESSION);
            }
            String userId = user.getName();
            String token = redisTemplate.opsForValue().get(TokenUtils.createTokenStoreKey(userId));
            if (!StringUtils.hasText(token)) {
                webSocketSessionManager.removeAll(Long.valueOf(userId), CloseStatus.NOT_ACCEPTABLE.withReason(WebsocketStatus.EXPIRED_TOKEN));
                throw new MessageDeliveryException(WebsocketStatus.EXPIRED_TOKEN);
            }
            JwtUtils.check(token);
        }
        return message;
    }

    private Message<?> createErrorMessage(String sessionId, Throwable throwable) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setSessionId(sessionId);
        return MessageBuilder.createMessage(throwable, errorAccessor.getMessageHeaders());
    }
}
