package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class GroupSessionApplyParam {

    private Long userId;

    private Long sessionId;

    private String applyInfo;
}
