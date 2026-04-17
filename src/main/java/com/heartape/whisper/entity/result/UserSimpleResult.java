package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.json.serializer.AvatarUrlSerializer;
import lombok.Data;

@Data
public class UserSimpleResult {

    private Long id;

    /** 用户名 */
    private String username;

    /** 头像URL */
    @JsonSerialize(using = AvatarUrlSerializer.class)
    private String avatar;

    /** 简介 */
    private String bio;

    public static UserSimpleResult of(User user) {
        UserSimpleResult userSimpleResult = new UserSimpleResult();
        userSimpleResult.setId(user.getId());
        userSimpleResult.setUsername(user.getUsername());
        userSimpleResult.setAvatar(user.getAvatar());
        userSimpleResult.setBio(user.getBio());
        return userSimpleResult;
    }
}

