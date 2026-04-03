package com.sp.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker // STOMP 웹소켓 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 프론트엔드(Vue)에서 처음 웹소켓을 연결할 주소: ws://localhost:9090/ws-chat
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*"); // CORS 전체 허용 (개발용)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. 구독(Subscribe)용 접두사: 클라이언트가 방에 입장할 때 사용
        // 예: /topic/channel/1 (1번 채널 구독)
        registry.enableSimpleBroker("/topic", "/queue");

        // 2. 발행(Publish)용 접두사: 클라이언트가 서버로 메시지를 보낼 때 사용
        // 예: /app/chat (서버로 채팅 전송)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 웹소켓 연결 시 JWT 토큰을 검증하기 위한 인터셉터 등록
        registration.interceptors(stompHandler);
    }
}