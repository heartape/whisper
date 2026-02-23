package com.heartape.whisper.service;

import com.heartape.whisper.entity.Param.UserParam;

public interface AuthService {
    Long login(String phone, String code);

    void logout(Long userId);

    void check(UserParam userParam);

    String token(Long id);
}
