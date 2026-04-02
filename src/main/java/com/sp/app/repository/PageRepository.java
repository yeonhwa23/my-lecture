package com.sp.app.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Page;

/**
 * pages 테이블
 */
public interface PageRepository extends JpaRepository<Page, Long> {

    /**
     * 워크스페이스의 루트 페이지 목록 (parent IS NULL, 미삭제)
     * sort_order 오름차순 정렬
     */
    List<Page> findByWorkspace_WorkspaceIdAndParentIsNullAndDeletedAtIsNullOrderBySortOrderAsc(
            Long workspaceId);

    /**
     * 특정 부모 페이지의 자식 페이지 목록 (미삭제)
     */
    List<Page> findByParent_PageIdAndDeletedAtIsNullOrderBySortOrderAsc(Long parentId);

    /**
     * 워크스페이스 전체 페이지 트리 한 번에 조회
     * (서비스 레이어에서 부모-자식 조립)
     */
    @Query("""
        SELECT p FROM Page p
        WHERE p.workspace.workspaceId = :workspaceId
          AND p.deletedAt IS NULL
        ORDER BY p.sortOrder ASC
    """)
    List<Page> findAllByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * 제목 검색 (워크스페이스 범위, 미삭제)
     */
    List<Page> findByWorkspace_WorkspaceIdAndTitleContainingAndDeletedAtIsNull(
            Long workspaceId, String keyword);

    /**
     * soft-delete 된 페이지 목록 (휴지통)
     */
    List<Page> findByWorkspace_WorkspaceIdAndDeletedAtIsNotNull(Long workspaceId);
}
