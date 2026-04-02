package com.sp.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.workspace.WorkspaceMember;

/**
 * workspace_members 테이블
 * PK = WorkspaceMember.WorkspaceMemberId (복합키)
 */
public interface WorkspaceMemberRepository
        extends JpaRepository<WorkspaceMember, WorkspaceMember.WorkspaceMemberId> {

    /** 워크스페이스의 전체 멤버 목록 */
    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);

    /** 특정 회원의 워크스페이스 내 정보 조회 */
    Optional<WorkspaceMember> findByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    boolean existsByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    /** 워크스페이스 멤버 수 */
    long countByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);
}
