package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class PeerSessionApplyParam {

    private Long applicantId;

    private Long reviewerId;

    private String aliasName;

    private String applyInfo;
}
