package com.heartape.whisper.service.impl;

import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.entity.result.UserSimpleResult;
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
    public User getById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public User getByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public void update(User user) {
        userMapper.update(user);
    }

    @Override
    public void create(UserParam userParam) {
        final User user = userParam.toUser();
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
}
