package com.heartape.whisper.controller;

import com.heartape.whisper.common.AuthenticationContext;
import com.heartape.whisper.common.Result;
import com.heartape.whisper.entity.Param.PasswordParam;
import com.heartape.whisper.entity.Param.RegisterParam;
import com.heartape.whisper.entity.Param.UserParam;
import com.heartape.whisper.entity.result.UserResult;
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
        UserResult user = userService.getById(userId);
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<?> user(@PathVariable Long id) {
        UserResult user = userService.getById(id);
        return Result.success(user);
    }

    @GetMapping("/find")
    public Result<?> find(@RequestParam String keyword) {
        Long userId = authenticationContext.getUserId();
        final List<UserSimpleResult> userSearchResults = userService.search(keyword, userId);
        return Result.success(userSearchResults);
    }

    @PostMapping
    public Result<?> create(@RequestBody RegisterParam registerParam) {
        userService.create(registerParam);
        return Result.success();
    }

    @PutMapping("/phone")
    public Result<?> bindPhone(@RequestBody UserParam userParam) {
        Long userId = authenticationContext.getUserId();
        userParam.setId(userId);
        userService.bindPhone(userParam);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<?> editPassword(@RequestBody PasswordParam passwordParam) {
        Long userId = authenticationContext.getUserId();
        passwordParam.setId(userId);
        userService.editPassword(passwordParam);
        return Result.success();
    }

    @PutMapping("/username")
    public Result<?> editUsername(@RequestBody UserParam userParam) {
        Long userId = authenticationContext.getUserId();
        userParam.setId(userId);
        userService.editUsername(userParam);
        return Result.success();
    }

    @PutMapping("/avatar")
    public Result<?> editAvatar(@RequestBody UserParam userParam) {
        Long userId = authenticationContext.getUserId();
        userParam.setId(userId);
        userService.editAvatar(userParam);
        return Result.success();
    }

    @PutMapping("/bio")
    public Result<?> editBio(@RequestBody UserParam userParam) {
        Long userId = authenticationContext.getUserId();
        userParam.setId(userId);
        userService.editBio(userParam);
        return Result.success();
    }

    @DeleteMapping
    public Result<?> delete() {
        Long userId = authenticationContext.getUserId();
        userService.delete(userId);
        return Result.success();
    }

}

