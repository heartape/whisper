package com.heartape.whisper.entity.result;

import com.heartape.whisper.common.constant.ApplyStatusEnum;
import com.heartape.whisper.common.constant.ApplyTypeEnum;
import com.heartape.whisper.entity.ImSessionApply;
import lombok.Data;

@Data
public class ImSessionApplyResult {

    private Long id;

    private ApplyTypeEnum type;

    private Long sessionId;

    private String aliasName;

    private Long applicantId;

    private Long reviewerId;

    private String applyInfo;

    private ApplyStatusEnum status;

    public static ImSessionApplyResult of(ImSessionApply imSessionApply) {
        ImSessionApplyResult result = new ImSessionApplyResult();
        result.setId(imSessionApply.getId());
        result.setType(imSessionApply.getType());
        result.setSessionId(imSessionApply.getSessionId());
        result.setAliasName(imSessionApply.getAliasName());
        result.setApplicantId(imSessionApply.getApplicantId());
        result.setReviewerId(imSessionApply.getReviewerId());
        result.setApplyInfo(imSessionApply.getApplyInfo());
        result.setStatus(imSessionApply.getStatus());
        return result;
    }
}
