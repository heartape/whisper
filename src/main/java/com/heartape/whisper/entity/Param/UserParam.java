package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class UserParam {

    /** 用户id */
    private Long id;

    /** 手机号 */
    private String phone;

    /** 用户名 */
    private String username;

    private String avatar;

    private String bio;

}

