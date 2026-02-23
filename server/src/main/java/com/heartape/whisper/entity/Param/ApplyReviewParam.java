package com.heartape.whisper.entity.Param;

import lombok.Data;

@Data
public class ApplyReviewParam {

    private Long applyId;

    private String aliasName;

    private Boolean approved;

    private String reviewNote;
}
