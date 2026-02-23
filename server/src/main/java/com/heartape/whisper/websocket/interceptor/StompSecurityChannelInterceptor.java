package com.heartape.whisper.websocket.interceptor;

import com.heartape.whisper.common.SensitiveWordFilter;
import com.heartape.whisper.common.SqlInjectionDetector;
import com.heartape.whisper.websocket.WebsocketStatus;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@Configuration
public class StompSecurityChannelInterceptor implements ChannelInterceptor {

    private final SensitiveWordFilter sensitiveWordFilter;
    private final SqlInjectionDetector sqlInjectionDetector;

    // 定义最大消息长度 (例如 10KB)，防止恶意发送超大包导致内存溢出
    private static final int MAX_MESSAGE_SIZE = 10 * 1024;

    @Override
    public Message<?> preSend(@SuppressWarnings("NullableProblems") Message<?> message, @SuppressWarnings("NullableProblems") MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command != StompCommand.SEND) {
            return message;
        }

        byte[] payload = (byte[]) message.getPayload();

        if (payload.length > MAX_MESSAGE_SIZE) {
            throw new MessageHandlingException(message, WebsocketStatus.MESSAGE_TOO_LARGE);
        }
        String payloadStr = new String(payload, StandardCharsets.UTF_8);

        if (sensitiveWordFilter.isContained(payloadStr)) {
            throw new MessageHandlingException(message, WebsocketStatus.CONTAINED_SENSITIVE_WORD);
        }
        if (sqlInjectionDetector.isValid(payloadStr)) {
            throw new MessageHandlingException(message, WebsocketStatus.SQL_INJECTION_SUSPECT);
        }
        return message;
    }
}
