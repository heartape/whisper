package com.heartape.whisper.service;

import com.heartape.whisper.entity.Param.RegisterParam;

public interface AuthService {
    Long login(String phone, String code);

    void logout(Long userId);

    void check(RegisterParam registerParam);

    String token(Long id);
}
