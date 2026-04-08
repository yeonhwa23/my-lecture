package com.sp.app.config;

import com.sp.app.security.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException; // ✨ 예외 처리용 import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@lombok.extern.slf4j.Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // 클라이언트가 처음 웹소켓 연결(CONNECT)을 요청할 때
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 헤더에서 "Authorization" 추출 (프론트에서 넣어줄 예정)
            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
                
                try {
                    // 토큰 검증 실패 시 예외 발생 -> 웹소켓 연결 강제 차단
                    if (!jwtTokenProvider.validateToken(jwtToken)) { 
                        log.error("웹소켓 연결 실패: 유효하지 않은 JWT 토큰");
                        throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
                    }
                } catch (ExpiredJwtException e) {
                    log.warn("웹소켓 연결 차단: 만료된 토큰이 요청되었습니다. (프론트 재로그인 필요)");
                    throw new IllegalArgumentException("만료된 토큰입니다.");
                } catch (Exception e) {
                    log.error("웹소켓 연결 차단: 토큰 검증 중 오류 발생 - {}", e.getMessage());
                    throw new IllegalArgumentException("토큰 검증 오류가 발생했습니다.");
                }
                
            } else {
                log.error("웹소켓 연결 실패: JWT 토큰 누락");
                throw new IllegalArgumentException("토큰이 존재하지 않습니다.");
            }
        }
        return message;
    }
}