package com.heartape.whisper.common;

public interface AuthenticationContext {
    void setUserId(Long userId);

    Long getUserId();

    void clear();
}
