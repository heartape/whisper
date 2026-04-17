package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.common.constant.RoleEnum;
import com.heartape.whisper.entity.User;
import com.heartape.whisper.json.serializer.AvatarUrlSerializer;
import lombok.Data;

@Data
public class UserResult {

    private Long id;

    /** 手机号 */
    private String phone;

    /** 用户名 */
    private String username;

    /** 头像URL */
    @JsonSerialize(using = AvatarUrlSerializer.class)
    private String avatar;

    /** 简介 */
    private String bio;

    private RoleEnum role;

    public static UserResult of(User user){
        final UserResult result = new UserResult();
        result.setId(user.getId());
        result.setPhone(user.getPhone());
        result.setUsername(user.getUsername());
        result.setAvatar(user.getAvatar());
        result.setBio(user.getBio());
        result.setRole(user.getRole());
        return result;
    }
}

