package com.sp.app.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Workspace;

/**
 * workspaces 테이블
 */
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    Optional<Workspace> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** 특정 회원이 속한 워크스페이스 목록 */
    @Query("""
        SELECT w FROM Workspace w
        JOIN WorkspaceMember wm ON wm.workspaceId = w.workspaceId
        WHERE wm.memberId = :memberId
        ORDER BY w.createdAt DESC
    """)
    List<Workspace> findByMemberId(@Param("memberId") Long memberId);
}
