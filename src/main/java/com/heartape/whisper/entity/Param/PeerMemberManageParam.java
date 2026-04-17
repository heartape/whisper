package com.heartape.whisper.entity.Param;

import com.heartape.whisper.common.constant.GroupMemberActionEnum;
import com.heartape.whisper.common.constant.PeerMemberActionEnum;
import lombok.Data;

@Data
public class PeerMemberManageParam {

    private Long sessionId;

    private Long operatorId;

    private Long userId;

    private String aliasName;

    private PeerMemberActionEnum action;
}
