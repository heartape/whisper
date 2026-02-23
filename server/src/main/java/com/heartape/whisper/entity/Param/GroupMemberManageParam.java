package com.heartape.whisper.entity.Param;

import com.heartape.whisper.common.constant.GroupMemberActionEnum;
import lombok.Data;

@Data
public class GroupMemberManageParam {

    private Long sessionId;

    private Long operatorId;

    private Long userId;

    private GroupMemberActionEnum action;
}
