package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.PageDto;

public interface PageService {

    /** 페이지 생성 */
    PageDto.Response createPage(Long workspaceId, PageDto.CreateRequest request, Long memberId);

    /** 워크스페이스 페이지 트리 조회 (사이드바용) */
    List<PageDto.TreeNode> getPageTree(Long workspaceId);

    /** 페이지 단건 조회 */
    PageDto.Response getPage(Long pageId);

    /** 페이지 내용 수정 (제목, 에디터 content, 아이콘 등) */
    PageDto.Response updatePage(Long pageId, PageDto.UpdateRequest request, Long memberId);

    /** 페이지 소프트 삭제 */
    void deletePage(Long pageId, Long memberId);

    /**
     * 채팅 메시지를 페이지 블록으로 추가 (북마크 기능)
     * - pages.content(JSON)에 텍스트 블록을 append
     * - bookmarks 테이블에 관계 기록
     */
    void bookmarkMessageToPage(Long pageId, PageDto.BookmarkRequest request, Long memberId);
}
