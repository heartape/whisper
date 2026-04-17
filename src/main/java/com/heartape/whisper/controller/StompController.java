package com.heartape.whisper.controller;

import com.heartape.whisper.entity.ImSessionMemberUser;
import com.heartape.whisper.entity.Param.ImMessageParam;
import com.heartape.whisper.entity.result.ImMessageResult;
import com.heartape.whisper.service.ImService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.encoder.Encode;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@AllArgsConstructor
@Controller
public class StompController {

    private final ImService imService;

    /**
     * 客户端发送路径: /app/session
     * 客户端订阅路径: /user/{user_id}/app/session
     */
    @MessageMapping("/session")
    public void handleSession(@Payload ImMessageParam message, Principal principal) {
        Long userId = Long.valueOf(principal.getName());
        message.setUserId(userId);
        imService.send(message);
    }

    @MessageExceptionHandler
    @SendToUser(destinations="/queue/error", broadcast=false)
    public String handleException(RuntimeException exception) {
        return exception.getMessage();
    }
}
