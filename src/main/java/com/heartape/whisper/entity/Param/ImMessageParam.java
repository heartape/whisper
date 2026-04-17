package com.heartape.whisper.entity.Param;

import com.heartape.whisper.common.constant.MessageEnum;
import com.heartape.whisper.entity.ImMessage;
import lombok.Data;

@Data
public class ImMessageParam {

    private Long sessionId;

    private Long userId;

    private MessageEnum messageType;

    /** 消息 */
    private String messageInfo;

    public ImMessage toImMessage() {
        ImMessage imMessage = new ImMessage();
        imMessage.setSessionId(sessionId);
        imMessage.setUserId(userId);
        imMessage.setMessageType(messageType);
        imMessage.setMessageInfo(messageInfo);
        return imMessage;
    }

}

