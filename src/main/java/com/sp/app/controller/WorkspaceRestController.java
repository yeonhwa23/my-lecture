package com.sp.app.controller;

import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;
import com.sp.app.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceRestController {

    private final WorkspaceService workspaceService;

    // ----------------------------------------------------------------
    // POST /api/workspaces — 워크스페이스 생성
    // ----------------------------------------------------------------
    @PostMapping
    public ResponseEntity<WorkspaceDto.Response> create(
            @RequestBody WorkspaceDto.CreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = getMemberId(userDetails);
        return ResponseEntity.ok(workspaceService.createWorkspace(request, memberId));
    }

    // ----------------------------------------------------------------
    // GET /api/workspaces — 내 워크스페이스 목록
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<WorkspaceDto.Response>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = getMemberId(userDetails);
        return ResponseEntity.ok(workspaceService.getMyWorkspaces(memberId));
    }

    // ----------------------------------------------------------------
    // GET /api/workspaces/check-slug?slug={slug} — 슬러그 중복 확인
    // ----------------------------------------------------------------
    @GetMapping("/check-slug")
    public ResponseEntity<WorkspaceDto.SlugCheckResponse> checkSlug(
            @RequestParam("slug") String slug) { // <-- 수정됨

        return ResponseEntity.ok(workspaceService.checkSlug(slug));
    }

    // ----------------------------------------------------------------
    // POST /api/workspaces/join — 초대 링크로 참여
    // ----------------------------------------------------------------
    @PostMapping("/join")
    public ResponseEntity<WorkspaceDto.Response> join(
            @RequestBody WorkspaceDto.JoinRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = getMemberId(userDetails);
        return ResponseEntity.ok(workspaceService.joinBySlug(request.getSlug(), memberId));
    }

    // ----------------------------------------------------------------
    // GET /api/workspaces/{workspaceId}/members — 멤버 목록
    // ----------------------------------------------------------------
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<WorkspaceMemberDto.Response>> getMembers(
            @PathVariable("workspaceId") Long workspaceId) { // <-- 수정됨

        return ResponseEntity.ok(workspaceService.getMembers(workspaceId));
    }

    // ----------------------------------------------------------------
    // DELETE /api/workspaces/{workspaceId}/members/{targetMemberId} — 강퇴
    // ----------------------------------------------------------------
    @DeleteMapping("/{workspaceId}/members/{targetMemberId}")
    public ResponseEntity<Void> kickMember(
            @PathVariable("workspaceId") Long workspaceId,       // <-- 수정됨
            @PathVariable("targetMemberId") Long targetMemberId, // <-- 수정됨
            @AuthenticationPrincipal UserDetails userDetails) {

        workspaceService.kickMember(workspaceId, targetMemberId, getMemberId(userDetails));
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // PATCH /api/workspaces/{workspaceId}/members/role — 역할 변경
    // ----------------------------------------------------------------
    @PatchMapping("/{workspaceId}/members/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable("workspaceId") Long workspaceId, // <-- 수정됨
            @RequestBody WorkspaceMemberDto.RoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        workspaceService.updateMemberRole(workspaceId, request, getMemberId(userDetails));
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // UserDetails → memberId 추출 헬퍼
    //   CustomUserDetails에 getMemberId()가 있으면 캐스팅해서 사용
    //   없으면 MemberRepository로 loginId → memberId 변환 필요
    // ----------------------------------------------------------------
    private Long getMemberId(UserDetails userDetails) {
        // CustomUserDetails에 getMemberId()가 있는 경우 ↓
        if (userDetails instanceof com.sp.app.security.CustomUserDetails custom) {
            return custom.getMemberId();
        }
        // 없는 경우: loginId로 memberId를 가져오는 방식 (아래 참고)
        throw new IllegalStateException("CustomUserDetails에 getMemberId()가 없습니다. 아래 코드를 추가하세요.");
    }
}