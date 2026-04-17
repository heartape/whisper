package com.heartape.whisper.service.impl;

import com.heartape.whisper.entity.Param.PasswordParam;
import com.heartape.whisper.entity.Param.RegisterParam;
import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.entity.result.UserResult;
import com.heartape.whisper.entity.result.UserSimpleResult;
import com.heartape.whisper.exception.BusinessException;
import com.heartape.whisper.mapper.UserMapper;
import com.heartape.whisper.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    @Override
    public UserResult getById(Long id) {
        final User user = userMapper.findById(id);
        return UserResult.of(user);
    }

    @Override
    public UserResult getByPhone(String phone) {
        final User user = userMapper.findByPhone(phone);
        return UserResult.of(user);
    }

    @Override
    public void create(RegisterParam registerParam) {
        final User user = registerParam.toUser();
        user.setBio("这个人很懒");
        user.setAvatar("https://picsum.photos/seed/u/200");
        userMapper.insert(user);
    }

    @Override
    public UserSimpleResult simple(Long id) {
        return userMapper.simple(id);
    }

    @Override
    public Map<Long, UserSimpleResult> simple(List<Long> ids) {
        List<UserSimpleResult> userSimpleResults = userMapper.simpleList(ids);
        return userSimpleResults.stream().collect(Collectors.toMap(UserSimpleResult::getId, Function.identity()));
    }

    @Override
    public List<UserSimpleResult> search(String keyword, Long userId) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        return userMapper.selectByMatchName(keyword, userId)
                .stream()
                .map(UserSimpleResult::of)
                .collect(Collectors.toList());
    }

    @Override
    public void bindPhone(UserParam userParam) {
        final String phone = userParam.getPhone();
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("手机号不能为空");
        }
        final User user = new User();
        user.setId(userParam.getId());
        user.setPhone(phone);
        userMapper.updateById(user);
    }

    @Override
    public void editPassword(PasswordParam passwordParam) {
        final String password = passwordParam.getPassword();
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("密码不能为空");
        }
        final User user = new User();
        user.setId(passwordParam.getId());
        user.setPhone(password);
        userMapper.updateById(user);
    }

    @Override
    public void editUsername(UserParam userParam) {
        final String username = userParam.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
        final User user = new User();
        user.setId(userParam.getId());
        user.setUsername(username);
        userMapper.updateById(user);
    }

    @Override
    public void editAvatar(UserParam userParam) {
        final String avatar = userParam.getAvatar();
        if (!StringUtils.hasText(avatar)) {
            throw new BusinessException("头像不能为空");
        }
        final User user = new User();
        user.setId(userParam.getId());
        user.setAvatar(avatar);
        userMapper.updateById(user);
    }

    @Override
    public void editBio(UserParam userParam) {
        final String bio = userParam.getBio();
        if (!StringUtils.hasText(bio)) {
            throw new BusinessException("简介不能为空");
        }
        final User user = new User();
        user.setId(userParam.getId());
        user.setBio(bio);
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long userId) {
        userMapper.delete(userId);
    }
}
