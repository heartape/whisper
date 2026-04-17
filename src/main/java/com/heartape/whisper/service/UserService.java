package com.heartape.whisper.service;

import com.heartape.whisper.entity.Param.PasswordParam;
import com.heartape.whisper.entity.Param.RegisterParam;
import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.entity.result.UserResult;
import com.heartape.whisper.entity.result.UserSimpleResult;

import java.util.List;
import java.util.Map;

public interface UserService {
    UserResult getById(Long id);
    UserResult getByPhone(String phone);
    void create(RegisterParam registerParam);

    UserSimpleResult simple(Long id);
    Map<Long, UserSimpleResult> simple(List<Long> ids);
    List<UserSimpleResult> search(String keyword, Long userId);

    void bindPhone(UserParam userParam);
    void editPassword(PasswordParam passwordParam);
    void editUsername(UserParam userParam);
    void editAvatar(UserParam userParam);
    void editBio(UserParam userParam);

    void delete(Long userId);

}