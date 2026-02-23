package com.heartape.whisper.mapper;

import com.heartape.whisper.entity.User;
import com.heartape.whisper.entity.result.UserSimpleResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User findById(Long id);
    User selectUsernameById(Long id);
    User findByPhone(String phone);
    int insert(User user);
    int update(User user);

    UserSimpleResult simple(Long id);

    List<UserSimpleResult> simpleList(List<Long> ids);
    List<User> selectByMatchName(String username, Long userId);
}

