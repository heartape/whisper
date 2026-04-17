package com.heartape.whisper.common;

import com.heartape.whisper.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
public class ThreadLocalAuthenticationContext implements AuthenticationContext {

    private static final ThreadLocal<Long> USER_HOLDER = new ThreadLocal<>();

    public void setUserId(Long userId) {
        USER_HOLDER.set(userId);
    }

    public Long getUserId() {
        final Long userId = USER_HOLDER.get();
        if (userId == null) {
            throw new UnauthorizedException("用户未登录");
        }
        return userId;
    }

    public void clear() {
        USER_HOLDER.remove();
    }
}
