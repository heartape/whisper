package com.heartape.whisper.service.impl;

import com.heartape.whisper.common.JwtUtils;
import com.heartape.whisper.common.TokenUtils;
import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.exception.UnauthorizedException;
import com.heartape.whisper.mapper.UserMapper;
import com.heartape.whisper.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    public Long login(String phone, String code) {
        // TODO: 验证码验证
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            throw new UnauthorizedException("登录失败");
        }
        return user.getId();
    }

    @Override
    public void logout(Long userId) {
        String tokenStoreKey = TokenUtils.createTokenStoreKey(String.valueOf(userId));
        redisTemplate.delete(tokenStoreKey);
    }

    @Override
    public void check(UserParam userParam) {

    }

    @Override
    public String token(Long id) {
        String idString = String.valueOf(id);
        String token = JwtUtils.generate(idString);
        redisTemplate.opsForValue().set("TOKEN:" + idString, token, 7, TimeUnit.DAYS);
        return TokenUtils.createBearerToken(token);
    }

}
