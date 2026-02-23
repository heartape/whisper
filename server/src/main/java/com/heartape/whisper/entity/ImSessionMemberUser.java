package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.GroupRoleEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImSessionMemberUser {

    private Long userId;

    private GroupRoleEnum role;

    private String aliasName;

    private String username;

    private String avatar;

    private Boolean isBlock;

    private LocalDateTime joinTime;

}
