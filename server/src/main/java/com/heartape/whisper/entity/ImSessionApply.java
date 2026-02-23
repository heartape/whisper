package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.ApplyBizTypeEnum;
import com.heartape.whisper.common.constant.ApplyStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImSessionApply {

    private Long id;

    private ApplyBizTypeEnum bizType;

    private Long sessionId;

    private String aliasName;

    private Long applicantId;

    private Long reviewerId;

    private String applyInfo;

    private ApplyStatusEnum status;

    private String reviewNote;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
