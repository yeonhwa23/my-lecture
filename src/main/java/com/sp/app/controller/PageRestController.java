package com.sp.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.sp.app.domain.dto.PageDto;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.service.PageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PageRestController {

    private final PageService pageService;

    // POST /api/workspaces/{workspaceId}/pages — 페이지 생성
    @PostMapping("/workspaces/{workspaceId}/pages")
    public ResponseEntity<PageDto.Response> createPage(
            @PathVariable("workspaceId") Long workspaceId,
            @RequestBody PageDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pageService.createPage(workspaceId, request, getMemberId(userDetails)));
    }

    // GET /api/workspaces/{workspaceId}/pages — 페이지 트리 조회 (사이드바)
    @GetMapping("/workspaces/{workspaceId}/pages")
    public ResponseEntity<List<PageDto.TreeNode>> getPageTree(
            @PathVariable("workspaceId") Long workspaceId) {

        return ResponseEntity.ok(pageService.getPageTree(workspaceId));
    }

    // GET /api/pages/{pageId} — 페이지 단건 조회 (에디터)
    @GetMapping("/pages/{pageId}")
    public ResponseEntity<PageDto.Response> getPage(
            @PathVariable("pageId") Long pageId) {

        return ResponseEntity.ok(pageService.getPage(pageId));
    }

    // PUT /api/pages/{pageId} — 페이지 수정 (에디터 자동저장)
    @PutMapping("/pages/{pageId}")
    public ResponseEntity<PageDto.Response> updatePage(
            @PathVariable("pageId") Long pageId,
            @RequestBody PageDto.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                pageService.updatePage(pageId, request, getMemberId(userDetails)));
    }

    // DELETE /api/pages/{pageId} — 페이지 소프트 삭제
    @DeleteMapping("/pages/{pageId}")
    public ResponseEntity<Void> deletePage(
            @PathVariable("pageId") Long pageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        pageService.deletePage(pageId, getMemberId(userDetails));
        return ResponseEntity.noContent().build();
    }

    // POST /api/pages/{pageId}/bookmark — 채팅 메시지를 페이지에 아카이빙
    @PostMapping("/pages/{pageId}/bookmark")
    public ResponseEntity<?> bookmarkMessage(
            @PathVariable("pageId") Long pageId,
            @RequestBody PageDto.BookmarkRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            pageService.bookmarkMessageToPage(pageId, request, getMemberId(userDetails));
            return ResponseEntity.ok(Map.of("message", "메시지가 페이지에 추가되었습니다."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    private Long getMemberId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetails custom) {
            return custom.getMemberId();
        }
        throw new IllegalStateException("인증 정보를 찾을 수 없습니다.");
    }
}