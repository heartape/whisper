package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class MessageSyncParam {

    private Long beforeMessageId;

    private Integer limit;
}
