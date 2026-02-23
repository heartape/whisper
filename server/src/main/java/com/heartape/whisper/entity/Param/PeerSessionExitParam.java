package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class PeerSessionExitParam {

    private Long userId;

    private Long sessionId;
}
