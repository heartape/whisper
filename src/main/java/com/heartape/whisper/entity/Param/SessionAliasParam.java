package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class SessionAliasParam {

    private Long sessionId;

    private Long userId;

    private String aliasName;
}
