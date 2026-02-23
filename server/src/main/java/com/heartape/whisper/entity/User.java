package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.RoleEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;

    /** 手机号 */
    private String phone;

    /** 用户名 */
    private String username;

    /** 加密后的密码 */
    private String password;

    /** 头像URL */
    private String avatar;

    /** 简介 */
    private String bio;

    private RoleEnum role;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

