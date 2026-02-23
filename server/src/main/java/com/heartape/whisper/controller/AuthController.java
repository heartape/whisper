package com.heartape.whisper.controller;

import com.heartape.whisper.common.AuthenticationContext;
import com.heartape.whisper.common.Result;
import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.service.AuthService;
import com.heartape.whisper.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @PostMapping("/register")
    public Result<?> register(@RequestBody UserParam userParam) {
        authService.check(userParam);
        userService.create(userParam);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String,String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        Long id = authService.login(phone, code);
        String bearerToken = authService.token(id);
        return Result.success(Map.of(HttpHeaders.AUTHORIZATION, bearerToken));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        Long userId = authenticationContext.getUserId();
        authService.logout(userId);
        return Result.success();
    }
}

