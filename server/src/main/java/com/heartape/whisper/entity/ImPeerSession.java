package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.PeerApplyEnum;
import lombok.Data;

@Data
public class ImPeerSession {
    private Long uid1;
    private Long uid2;
    private Long sessionId;
    private PeerApplyEnum applyStatus;
}
