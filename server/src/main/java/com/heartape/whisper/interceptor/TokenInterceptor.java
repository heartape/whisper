package com.heartape.whisper.interceptor;

import com.heartape.whisper.common.AuthenticationContext;
import com.heartape.whisper.common.JwtUtils;
import com.heartape.whisper.common.TokenUtils;
import com.heartape.whisper.exception.UnauthorizedException;
import com.heartape.whisper.websocket.WebsocketStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@AllArgsConstructor
@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    private final AuthenticationContext authenticationContext;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken == null || bearerToken.isEmpty()) {
            throw new UnauthorizedException("登录已过期，请重新登录");
        }

        String token = TokenUtils.getTokenFromBearerToken(bearerToken);
        if (token.isEmpty()) {
            throw new UnauthorizedException("登录已过期，请重新登录");
        }

        Claims claims;
        try {
            claims = JwtUtils.parse(token);
        } catch (ExpiredJwtException e) {
            throw new UnauthorizedException(WebsocketStatus.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException | IllegalArgumentException | SignatureException | MalformedJwtException e) {
            throw new UnauthorizedException(WebsocketStatus.INVALID_TOKEN);
        }
        String id = claims.getSubject();

        String tokenStore = redisTemplate.opsForValue().get(TokenUtils.createTokenStoreKey(id));
        // 只允许一个设备登录
        if (!token.equals(tokenStore)) {
            throw new UnauthorizedException(WebsocketStatus.EXPIRED_TOKEN);
        }

        authenticationContext.setUserId(Long.valueOf(id));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        authenticationContext.clear();
    }
}
