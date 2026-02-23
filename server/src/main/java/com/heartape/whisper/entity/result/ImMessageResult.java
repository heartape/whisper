package com.heartape.whisper.entity.result;

import com.heartape.whisper.common.constant.MessageEnum;
import com.heartape.whisper.entity.ImMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ImMessageResult {

    private Long id;

    private Long sessionId;

    /** 发送者 */
    private Long userId;

    private MessageEnum messageType;

    /** 信息 */
    private String messageInfo;

    private LocalDateTime createTime;

    public static ImMessageResult of(ImMessage imMessage) {
        ImMessageResult result = new ImMessageResult();
        result.setId(imMessage.getId());
        result.setSessionId(imMessage.getSessionId());
        result.setUserId(imMessage.getUserId());
        result.setMessageType(imMessage.getMessageType());
        result.setMessageInfo(imMessage.getMessageInfo());
        result.setCreateTime(imMessage.getCreateTime());
        return result;
    }

}

