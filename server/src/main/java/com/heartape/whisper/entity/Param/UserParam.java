package com.heartape.whisper.entity.Param;

import com.heartape.whisper.entity.User;
import lombok.Data;

@Data
public class UserParam {

    /** 手机号 */
    private String phone;

    /** 用户名 */
    private String username;

    private String password;

    public User toUser() {
        final User user = new User();
        user.setPhone(phone);
        user.setUsername(username);
        user.setPassword(password);
        return user;
    }
}

