package com.sp.app.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.FileManager;
import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.service.WorkspaceService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceRestController {

    private final WorkspaceService workspaceService;
    private final FileManager fileManager;

    // ----------------------------------------------------------------
    // POST /api/workspaces — 워크스페이스 생성 (JSON 또는 multipart 모두 지원)
    // ----------------------------------------------------------------
    @PostMapping(consumes = { "application/json", "multipart/form-data" })
    public ResponseEntity<WorkspaceDto.Response> create(
            @RequestParam(value = "wsName")                          String wsName,
            @RequestParam(value = "slug")                            String slug,
            @RequestParam(value = "description",  required = false)  String description,
            @RequestParam(value = "iconString",   required = false)  String iconString,
            @RequestParam(value = "iconFile",      required = false)  MultipartFile iconFile,
            @RequestParam(value = "bannerFile",    required = false)  MultipartFile bannerFile,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {

        Long memberId = getMemberId(userDetails);

        String pathname = "C:" + File.separator + "uploads" + File.separator + "workspace";
        new File(pathname).mkdirs();

        // 아이콘 결정: 파일 > 이모지 문자열
        String iconUrl = null;
        if (iconFile != null && !iconFile.isEmpty()) {
            String saved = fileManager.doFileUpload(iconFile, pathname);
            iconUrl = "/uploads/workspace/" + saved;
        } else if (iconString != null && !iconString.isBlank()) {
            iconUrl = iconString;
        }

        // 배너
        String bannerUrl = null;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            String saved = fileManager.doFileUpload(bannerFile, pathname);
            bannerUrl = "/uploads/workspace/" + saved;
        }

        WorkspaceDto.CreateRequest request = WorkspaceDto.CreateRequest.builder()
                .wsName(wsName)
                .slug(slug)
                .description(description)
                .iconUrl(iconUrl)
                .bannerUrl(bannerUrl)
                .build();

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
    // 워크 스페이스 수정 (방장만)
    // ----------------------------------------------------------------
    @PutMapping("/update")
    public ResponseEntity<?> updateWorkspace(
            @RequestParam("workspaceId") Long workspaceId,
            @RequestParam("wsName") String wsName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "iconString", required = false) String iconString, 
            @RequestParam(value = "iconFile", required = false) MultipartFile iconFile, // 실제 사진
            @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
            @RequestParam(value = "removeBanner", required = false, defaultValue = "false") boolean removeBanner,
            Authentication auth) { //

        try {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long requestMemberId = userDetails.getMemberId();

            // 🚨 서버가 재시작되어도 절대 지워지지 않는 C드라이브 안전한 경로로 고정!
            String pathname = "C:" + File.separator + "uploads" + File.separator + "workspace";
            
            File f = new File(pathname);
            if(!f.exists()) f.mkdirs();

            // 디버깅용: 파일이 제대로 넘어왔는지 서버 로그(콘솔)에 찍어봅니다.
            if (iconFile != null && !iconFile.isEmpty()) {
                System.out.println("✅ 수신된 아이콘 파일명: " + iconFile.getOriginalFilename());
            } else {
                System.out.println("❌ 아이콘 파일 없음 (또는 이모지 사용)");
            }

            workspaceService.updateWorkspaceSettings(
                workspaceId, requestMemberId, wsName, description, 
                iconString, iconFile, bannerFile, removeBanner, pathname
            );

            return ResponseEntity.ok(Map.of("message", "워크스페이스가 성공적으로 업데이트되었습니다."));
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", se.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 에러 발생 시 콘솔에 상세 에러 출력!
            return ResponseEntity.internalServerError().body(Map.of("message", "오류가 발생했습니다."));
        }
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