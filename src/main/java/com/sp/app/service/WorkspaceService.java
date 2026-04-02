package com.sp.app.service;

import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;

import java.util.List;

public interface WorkspaceService {

    /** 워크스페이스 생성 (생성자를 owner로 workspace_members에 함께 INSERT) */
    WorkspaceDto.Response createWorkspace(WorkspaceDto.CreateRequest request, Long loginMemberId);

    /** 내가 속한 워크스페이스 목록 조회 */
    List<WorkspaceDto.Response> getMyWorkspaces(Long loginMemberId);

    /** 슬러그 중복 여부 확인 */
    WorkspaceDto.SlugCheckResponse checkSlug(String slug);

    /** 슬러그(초대 링크)로 워크스페이스 참여 */
    WorkspaceDto.Response joinBySlug(String slug, Long loginMemberId);

    /** 워크스페이스 멤버 목록 조회 */
    List<WorkspaceMemberDto.Response> getMembers(Long workspaceId);

    /** 멤버 강퇴 (ws_role이 owner/admin만 가능) */
    void kickMember(Long workspaceId, Long targetMemberId, Long requestMemberId);

    /** 멤버 역할 변경 (owner만 가능) */
    void updateMemberRole(Long workspaceId, WorkspaceMemberDto.RoleUpdateRequest request, Long requestMemberId);
}
