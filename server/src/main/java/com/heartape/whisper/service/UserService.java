package com.heartape.whisper.service;

import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.entity.result.UserSimpleResult;

import java.util.List;
import java.util.Map;

public interface UserService {
    User getById(Long id);
    User getByPhone(String phone);
    void update(User user);
    void create(UserParam userParam);

    UserSimpleResult simple(Long id);
    Map<Long, UserSimpleResult> simple(List<Long> ids);
    List<UserSimpleResult> search(String keyword, Long userId);
}