// ── PageRepository.java ──────────────────────────────────────────────
package com.sp.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Page;

public interface PageRepository extends JpaRepository<Page, Long> {

    // 워크스페이스 내 삭제되지 않은 전체 페이지 (트리 구성용)
    @Query("SELECT p FROM Page p WHERE p.workspace.workspaceId = :wsId AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC, p.createdAt ASC")
    List<Page> findAllByWorkspaceId(@Param("wsId") Long workspaceId);

    // 최상위 페이지만 (parent IS NULL)
    @Query("SELECT p FROM Page p WHERE p.workspace.workspaceId = :wsId AND p.parent IS NULL AND p.deletedAt IS NULL ORDER BY p.sortOrder ASC")
    List<Page> findRootPagesByWorkspaceId(@Param("wsId") Long workspaceId);

    // 삭제되지 않은 단건 조회
    @Query("SELECT p FROM Page p WHERE p.pageId = :pageId AND p.deletedAt IS NULL")
    Optional<Page> findActiveById(@Param("pageId") Long pageId);
}
