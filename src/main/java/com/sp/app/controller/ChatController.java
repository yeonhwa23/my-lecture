package com.sp.app.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.service.WorkspaceService;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
	
	private final WorkspaceService workspaceService;

    @MessageMapping("/chat/{channelId}")
    @SendTo("/topic/channel/{channelId}")
    public ChatMessageResponse sendMessage(@DestinationVariable("channelId") Long channelId, 
                                           ChatMessageRequest request) {
        
    	workspaceService.saveChatMessage(channelId, request.getMemberId(), request.getContent());
        
        ChatMessageResponse response = new ChatMessageResponse();
        response.setChannelId(channelId);
        response.setMemberId(request.getMemberId());     // ✅ 아이디 대신 고유번호(PK) 사용
        response.setSenderName(request.getSenderName());
        response.setContent(request.getContent());
        response.setSendTime(LocalDateTime.now().toString());

        return response; 
    }

    @GetMapping("/api/workspaces/channel/{channelId}/messages")
    public List<ChatMessageResponse> getChatHistory(@PathVariable("channelId") Long channelId) {

    	return workspaceService.getChatHistory(channelId);
    }

    @Data
    public static class ChatMessageRequest {
        private Long memberId;     // ✅ 수정됨
        private String senderName;
        private String content;
    }

    @Data
    public static class ChatMessageResponse {
        private Long channelId;
        private Long memberId;     // ✅ 수정됨
        private String senderName;
        private String content;
        private String sendTime;
    }
}