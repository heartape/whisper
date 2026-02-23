package com.heartape.whisper.entity;

import com.heartape.whisper.common.constant.MessageEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ImMessage {

    private Long id;

    private Long sessionId;

    /** 发送者 */
    private Long userId;

    private MessageEnum messageType;

    /** 信息 */
    private String messageInfo;

    private LocalDateTime createTime;
}

