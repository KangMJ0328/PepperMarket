package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커를 활성화하는 애노테이션
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 간단한 메세지 브로커를 활성화하고 "/topic"으로 시작하는 목적지에 메시지를 브로커가 처리하도록 설정
        config.enableSimpleBroker("/topic");
        // 애플리케이션에서 목적지 경로를 "/app"으로 시작하도록 설정
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // "/ws/chat" 엔드포인트를 STOMP 엔드포인트로 등록하고 SockJS 지원을 추가
        registry.addEndpoint("/ws/chat").withSockJS();
    }
}
