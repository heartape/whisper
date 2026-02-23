package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class GroupAnnouncementParam {

    private Long sessionId;

    private Long userId;

    private String content;
}
