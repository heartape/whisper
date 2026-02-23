package com.heartape.whisper.controller;

import com.heartape.whisper.entity.Param.ImMessageParam;
import com.heartape.whisper.entity.result.ImMessageResult;
import com.heartape.whisper.service.ImService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@AllArgsConstructor
@Controller
public class StompController {

    private final ImService imService;

    /**
     * 单聊发送 (P2P)
     * 客户端发送路径: /app/peer/session.{sessionId}
     */
    @MessageMapping("/peer/session.{sessionId}")
    @SendTo("/queue/session.{sessionId}")
    public ImMessageResult handlePeerChat(@DestinationVariable Long sessionId, @Payload ImMessageParam message, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        message.setUserId(userId);
        message.setSessionId(sessionId);
        ImMessageResult imMessageResult = imService.send(message);
        log.info("P2P消息: 从user: {} 发给session: {}", userId, sessionId);

        String messageInfo = imMessageResult.getMessageInfo();
        String safeMessageInfo = Encode.forHtml(messageInfo);
        message.setMessageInfo(safeMessageInfo);
        return imMessageResult;
    }

    /**
     * 群聊发送
     * 客户端发送路径: /app/group/session.{sessionId}
     */
    @MessageMapping("/group/session.{sessionId}")
    @SendTo("/topic/session.{sessionId}")
    public ImMessageResult handleGroupChat(@DestinationVariable Long sessionId, @Payload ImMessageParam message, Principal principal) {
        // 获取发送者身份 (从安全上下文获取，更安全)
        Long userId = Long.valueOf(principal.getName());
        message.setUserId(userId);
        message.setSessionId(sessionId);
        ImMessageResult imMessageResult = imService.send(message);
        log.info("Group消息: 从user: {} 发给session: {}", userId, sessionId);

        String messageInfo = imMessageResult.getMessageInfo();
        String safeMessageInfo = Encode.forHtml(messageInfo);
        message.setMessageInfo(safeMessageInfo);
        return imMessageResult;
    }

    @MessageExceptionHandler
    @SendToUser(destinations="/queue/error", broadcast=false)
    public String handleException(RuntimeException exception) {
        return exception.getMessage();
    }
}
