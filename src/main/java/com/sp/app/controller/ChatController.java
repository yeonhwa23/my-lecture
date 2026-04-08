package com.sp.app.controller;

import java.time.LocalDateTime;
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
    public ChatMessageResponse sendMessage(
            @DestinationVariable("channelId") Long channelId,
            ChatMessageRequest request) {

        // ✅ 파일만 보낼 때 content가 null이면 ORA-01400 발생 → 빈 문자열로 대체
    	String content = (request.getContent() != null && !request.getContent().isBlank())
    	        ? request.getContent()
    	        : "(파일 첨부)"; 

        // ✅ 첨부파일 정보를 함께 저장 (imageUrl / fileUrl / fileName)
        Long savedMessageId = workspaceService.saveChatMessage(
                channelId,
                request.getMemberId(),
                content,
                request.getImageUrl(),
                request.getFileUrl(),
                request.getFileName()
        );

        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessageId(savedMessageId);
        response.setChannelId(channelId);
        response.setMemberId(request.getMemberId());
        response.setSenderName(request.getSenderName());
        response.setContent(content);
        response.setImageUrl(request.getImageUrl());
        response.setFileUrl(request.getFileUrl());
        response.setFileName(request.getFileName());
        response.setSendTime(LocalDateTime.now().toString());

        return response;
    }

    @GetMapping("/api/workspaces/channel/{channelId}/messages")
    public List<ChatMessageResponse> getChatHistory(
            @PathVariable("channelId") Long channelId) {
        return workspaceService.getChatHistory(channelId);
    }

    // ──────────────────────────────────────────────────────────
    //  Request / Response DTO
    // ──────────────────────────────────────────────────────────

    @Data
    public static class ChatMessageRequest {
        private Long   memberId;
        private String senderName;
        private String content;
        private String imageUrl;
        private String fileUrl;
        private String fileName;
    }

    @Data
    public static class ChatMessageResponse {
        private Long   messageId;
        private Long   channelId;
        private Long   memberId;
        private String senderName;
        private String content;
        private String imageUrl;
        private String fileUrl;
        private String fileName;
        private String sendTime;
    }
}