package com.heartape.whisper.websocket.interceptor;

import com.heartape.whisper.util.JwtUtils;
import com.heartape.whisper.util.TokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@AllArgsConstructor
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String bearToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!TokenUtils.isBearerToken(bearToken)){
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        String token = TokenUtils.getTokenFromBearerToken(bearToken);
        Claims claims;
        try {
            claims = JwtUtils.parse(token);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        String subject = claims.getSubject();
        String redisToken = redisTemplate.opsForValue().get(TokenUtils.createTokenStoreKey(subject));
        // 目前仅支持单客户端
        if (!redisToken.equals(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}
