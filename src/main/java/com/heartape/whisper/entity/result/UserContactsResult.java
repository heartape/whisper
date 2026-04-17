package com.heartape.whisper.entity.result;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.heartape.whisper.json.serializer.AvatarUrlSerializer;
import lombok.Data;

@Data
public class UserContactsResult {

    private Long id;

    /** 用户名 */
    private String username;

    /** 头像URL */
    @JsonSerialize(using = AvatarUrlSerializer.class)
    private String avatar;
}

