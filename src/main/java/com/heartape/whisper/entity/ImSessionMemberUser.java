package com.heartape.whisper.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.common.constant.GroupRoleEnum;
import com.heartape.whisper.json.serializer.AvatarUrlSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImSessionMemberUser {

    private Long userId;

    private GroupRoleEnum role;

    private String aliasName;

    private String username;

    @JsonSerialize(using = AvatarUrlSerializer.class)
    private String avatar;

    private Boolean isBlock;

    private Long joinTime;

}
