package com.heartape.whisper.controller;

import com.heartape.whisper.common.AuthenticationContext;
import com.heartape.whisper.common.Result;
import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.entity.result.UserSimpleResult;
import com.heartape.whisper.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthenticationContext authenticationContext;

    @GetMapping
    public Result<?> user() {
        Long userId = authenticationContext.getUserId();
        User user = userService.getById(userId);
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<?> user(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @GetMapping("/find")
    public Result<?> find(@RequestParam String keyword) {
        Long userId = authenticationContext.getUserId();
        final List<UserSimpleResult> userSearchResults = userService.search(keyword, userId);
        return Result.success(userSearchResults);
    }

    @PostMapping
    public Result<?> create(@RequestBody UserParam userParam) {
        userService.create(userParam);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody User user) {
        userService.update(user);
        return Result.success();
    }

}

