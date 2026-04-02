package com.sp.app.repository;

import com.sp.app.entity.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * workspace_members 테이블
 * @IdClass 방식이므로 findByWorkspaceId 등 필드명 직접 사용 가능
 */
public interface WorkspaceMemberRepository
        extends JpaRepository<WorkspaceMember, WorkspaceMember.WorkspaceMemberId> {

    List<WorkspaceMember> findByWorkspaceId(Long workspaceId);

    Optional<WorkspaceMember> findByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    boolean existsByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);

    long countByWorkspaceId(Long workspaceId);

    void deleteByWorkspaceIdAndMemberId(Long workspaceId, Long memberId);
}