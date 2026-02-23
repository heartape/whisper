package com.heartape.whisper.common;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalAuthenticationContext implements AuthenticationContext {

    private static final ThreadLocal<Long> USER_HOLDER = new ThreadLocal<>();

    public void setUserId(Long userId) {
        USER_HOLDER.set(userId);
    }

    public Long getUserId() {
        return USER_HOLDER.get();
    }

    public void clear() {
        USER_HOLDER.remove();
    }
}
