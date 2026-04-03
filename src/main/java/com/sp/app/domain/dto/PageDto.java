package com.sp.app.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.sp.app.entity.workspace.Page;

import lombok.*;

public class PageDto {

    // ── 생성 요청 ──────────────────────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateRequest {
        private String  title;
        private Long    parentId;    // null이면 최상위 페이지
        private String  iconEmoji;
    }

    // ── 수정 요청 ──────────────────────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateRequest {
        private String  title;
        private String  content;     // Tiptap JSON
        private String  iconEmoji;
        private Integer isPublic;
    }

    // ── 북마크(채팅→페이지) 요청 ───────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    public static class BookmarkRequest {
        private Long   messageId;
        private String content;      // 채팅 메시지 텍스트
    }

    // ── 응답 ──────────────────────────────────────────────────────────
    @Getter @Builder
    public static class Response {
        private Long            pageId;
        private Long            workspaceId;
        private Long            parentId;
        private String          title;
        private String          content;
        private String          iconEmoji;
        private Integer         isPublic;
        private Integer         sortOrder;
        private String          createdByName;
        private LocalDateTime   createdAt;
        private LocalDateTime   updatedAt;

        public static Response from(Page page) {
            return Response.builder()
                    .pageId(page.getPageId())
                    .workspaceId(page.getWorkspace().getWorkspaceId())
                    .parentId(page.getParent() != null ? page.getParent().getPageId() : null)
                    .title(page.getTitle())
                    .content(page.getContent())
                    .iconEmoji(page.getIconEmoji())
                    .isPublic(page.getIsPublic())
                    .sortOrder(page.getSortOrder())
                    .createdByName(page.getCreatedBy() != null ? page.getCreatedBy().getLoginId() : "알 수 없음")
                    .createdAt(page.getCreatedAt())
                    .updatedAt(page.getUpdatedAt())
                    .build();
        }
    }

    // ── 트리 응답 (사이드바용) ─────────────────────────────────────────
    @Getter @Builder
    public static class TreeNode {
        private Long            pageId;
        private String          title;
        private String          iconEmoji;
        private Long            parentId;
        private Integer         sortOrder;
        private List<TreeNode>  children;

        public static TreeNode from(Page page, List<TreeNode> children) {
            return TreeNode.builder()
                    .pageId(page.getPageId())
                    .title(page.getTitle())
                    .iconEmoji(page.getIconEmoji())
                    .parentId(page.getParent() != null ? page.getParent().getPageId() : null)
                    .sortOrder(page.getSortOrder())
                    .children(children)
                    .build();
        }
    }
}
