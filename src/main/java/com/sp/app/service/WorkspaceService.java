package com.sp.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.sp.app.controller.ChatController;
import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;

public interface WorkspaceService {

    /** 워크스페이스 생성 */
    WorkspaceDto.Response createWorkspace(WorkspaceDto.CreateRequest request, Long loginMemberId);

    /** 워크스페이스 설정 업데이트 (방장 전용) */
    void updateWorkspaceSettings(Long workspaceId, Long requestMemberId, String wsName, String description,
            String iconString, MultipartFile iconFile, MultipartFile bannerFile, boolean removeBanner, String pathname) throws Exception;

    /** 내가 속한 워크스페이스 목록 조회 */
    List<WorkspaceDto.Response> getMyWorkspaces(Long loginMemberId);

    /** 슬러그 중복 여부 확인 */
    WorkspaceDto.SlugCheckResponse checkSlug(String slug);

    /** 슬러그(초대 링크)로 워크스페이스 참여 */
    WorkspaceDto.Response joinBySlug(String slug, Long loginMemberId);

    /** 워크스페이스 멤버 목록 조회 */
    List<WorkspaceMemberDto.Response> getMembers(Long workspaceId);

    /** 멤버 강퇴 */
    void kickMember(Long workspaceId, Long targetMemberId, Long requestMemberId);

    /** 멤버 역할 변경 */
    void updateMemberRole(Long workspaceId, WorkspaceMemberDto.RoleUpdateRequest request, Long requestMemberId);

    /**
     * 채팅 메시지 저장 (첨부파일 포함)
     * - content가 null이면 빈 문자열로 저장 (파일만 보낼 때 ORA-01400 방지)
     * - imageUrl / fileUrl / fileName 있으면 message_attachments에 함께 저장
     */
    Long saveChatMessage(Long channelId, Long memberId, String content,
                         String imageUrl, String fileUrl, String fileName);

    /** 이전 채팅 내역 불러오기 */
    List<ChatController.ChatMessageResponse> getChatHistory(Long channelId);
}