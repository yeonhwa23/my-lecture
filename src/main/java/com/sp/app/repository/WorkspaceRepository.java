package com.sp.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Workspace;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    /** 슬러그 중복 확인 */
    boolean existsBySlug(String slug);

    /** 슬러그로 워크스페이스 조회 (초대 링크 입장용) */
    Optional<Workspace> findBySlug(String slug);

    /**
     * 로그인한 회원이 참여 중인 워크스페이스 목록
     * workspace_members 테이블을 통해 조인
     */
    @Query("""
        SELECT w FROM Workspace w
        JOIN w.workspaceMembers wm
        WHERE wm.member.memberId = :memberId
        ORDER BY w.createdAt DESC
    """)
    List<Workspace> findAllByMemberId(@Param("memberId") Long memberId);
}
