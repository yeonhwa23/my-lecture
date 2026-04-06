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

        // ✅ 저장 후 생성된 messageId를 반환받아야 북마크 가능
        // WorkspaceService.saveChatMessage() 가 저장된 Message 엔티티(또는 ID)를 반환하도록 수정 필요
        Long savedMessageId = workspaceService.saveChatMessage(
                channelId, request.getMemberId(), request.getContent());

        ChatMessageResponse response = new ChatMessageResponse();
        response.setMessageId(savedMessageId);        // ✅ 핵심: messageId 포함
        response.setChannelId(channelId);
        response.setMemberId(request.getMemberId());
        response.setSenderName(request.getSenderName());
        response.setContent(request.getContent());
        response.setImageUrl(request.getImageUrl());  // ✅ 이미지 URL
        response.setFileUrl(request.getFileUrl());    // ✅ 파일 URL
        response.setFileName(request.getFileName());  // ✅ 파일명
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
        private String imageUrl;   // ✅ 추가
        private String fileUrl;    // ✅ 추가
        private String fileName;   // ✅ 추가
    }

    @Data
    public static class ChatMessageResponse {
        private Long   messageId;  // ✅ 핵심: 북마크에 필요
        private Long   channelId;
        private Long   memberId;
        private String senderName;
        private String content;
        private String imageUrl;   // ✅ 추가
        private String fileUrl;    // ✅ 추가
        private String fileName;   // ✅ 추가
        private String sendTime;
    }
}