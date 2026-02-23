package com.heartape.whisper.websocket;

import com.heartape.whisper.websocket.interceptor.StompRateLimiterChannelInterceptor;
import com.heartape.whisper.websocket.interceptor.StompSecurityChannelInterceptor;
import com.heartape.whisper.websocket.interceptor.StompTokenChannelInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.security.Principal;

/**
 * @see <a href="https://docs.spring.io/spring-framework/reference/web/websocket/stomp/enable.html">docs.spring.io</a>
 */
@AllArgsConstructor
@Configuration
@EnableWebSocketMessageBroker
public class StompWebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final StompRateLimiterChannelInterceptor rateLimiterChannelInterceptor;
    private final StompTokenChannelInterceptor tokenChannelInterceptor;
    private final StompSecurityChannelInterceptor securityChannelInterceptor;

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry
                // 设置发送消息的超时时间。如果心跳包因为网络拥塞在这个时间内发不出去，连接会被关闭。
                .setSendTimeLimit(15 * 1000)
                // 设置发送缓冲区大小。心跳包虽然小，但如果业务数据堆积超过这个值，会影响心跳发送。
                .setSendBufferSizeLimit(512 * 1024)
                // 设置单条消息最大长度
                .setMessageSizeLimit(128 * 1024);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                rateLimiterChannelInterceptor,
                tokenChannelInterceptor,
                securityChannelInterceptor
        );
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                // 服务端的访问路径
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry
                .setUserDestinationPrefix("/user")
                // 服务端地址,@MessageMapping消息接收路径
                .setApplicationDestinationPrefixes("/app")
                // 客户端地址，@SendTo 和 SimpMessagingTemplat消息发送路径
                .enableSimpleBroker("/topic", "/queue")
                // 心跳
                .setTaskScheduler(heartbeatTaskScheduler())
                .setHeartbeatValue(new long[]{10000, 10000});
    }

    // 定义专门处理心跳的线程池
    @Bean
    public TaskScheduler heartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Runtime.getRuntime().availableProcessors()); // 根据CPU核心数设置
        scheduler.setThreadNamePrefix("ws-heartbeat-thread-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public WebSocketHandlerDecoratorFactory decoratorFactory(WebSocketSessionManager webSocketSessionManager) {
        return handler -> new WebSocketHandlerDecorator(handler) {

            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                // 1. 连接建立时，从 Principal 获取用户 ID
                Principal principal = session.getPrincipal();
                if (principal != null) {
                    webSocketSessionManager.register(Long.valueOf(principal.getName()), session);
                }
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                // 2. 连接断开时，移除 Session
                Principal principal = session.getPrincipal();
                if (principal != null) {
                    webSocketSessionManager.remove(Long.valueOf(principal.getName()), session);
                }
                super.afterConnectionClosed(session, closeStatus);
            }
        };
    }

}
