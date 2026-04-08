package com.sp.app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode; 
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.app.domain.dto.PageDto;
import com.sp.app.entity.member.Member;
import com.sp.app.entity.workspace.Bookmark;
import com.sp.app.entity.workspace.Message;
import com.sp.app.entity.workspace.Page;
import com.sp.app.entity.workspace.Workspace;
import com.sp.app.repository.BookmarkRepository;
import com.sp.app.repository.MemberRepository;
import com.sp.app.repository.MessageRepository;
import com.sp.app.repository.PageRepository;
import com.sp.app.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PageServiceImpl implements PageService {

    private final PageRepository       pageRepository;
    private final BookmarkRepository   bookmarkRepository;
    private final WorkspaceRepository  workspaceRepository;
    private final MemberRepository     memberRepository;
    private final MessageRepository    messageRepository;
    private final ObjectMapper         objectMapper;

    // ── 페이지 생성 ────────────────────────────────────────────────────
    @Override
    @Transactional
    public PageDto.Response createPage(Long workspaceId, PageDto.CreateRequest request, Long memberId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 워크스페이스입니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 부모 페이지 설정 (있으면)
        Page parent = null;
        if (request.getParentId() != null) {
            parent = pageRepository.findActiveById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 페이지를 찾을 수 없습니다."));

            // 최대 3단계 깊이 체크
            if (getDepth(parent) >= 3) {
                throw new IllegalArgumentException("페이지는 최대 3단계까지만 중첩할 수 있습니다.");
            }
        }

        Page page = Page.builder()
                .workspace(workspace)
                .parent(parent)
                .createdBy(member)
                .title(request.getTitle() != null ? request.getTitle() : "제목 없는 페이지")
                .iconEmoji(request.getIconEmoji())
                .content("{\"type\":\"doc\",\"content\":[]}") // ✨ [수정됨] Tiptap 에디터의 기본 JSON 형식으로 초기화
                .build();

        pageRepository.save(page);
        return PageDto.Response.from(page);
    }

    // ── 페이지 트리 조회 ───────────────────────────────────────────────
    @Override
    public List<PageDto.TreeNode> getPageTree(Long workspaceId) {
        List<Page> allPages = pageRepository.findAllByWorkspaceId(workspaceId);

        // parentId → children 맵 구성
        Map<Long, List<Page>> childrenMap = allPages.stream()
                .filter(p -> p.getParent() != null)
                .collect(Collectors.groupingBy(p -> p.getParent().getPageId()));

        // 최상위 페이지부터 재귀적으로 트리 구성
        return allPages.stream()
                .filter(p -> p.getParent() == null)
                .map(p -> buildTreeNode(p, childrenMap))
                .collect(Collectors.toList());
    }

    private PageDto.TreeNode buildTreeNode(Page page, Map<Long, List<Page>> childrenMap) {
        List<PageDto.TreeNode> children = childrenMap
                .getOrDefault(page.getPageId(), List.of())
                .stream()
                .map(child -> buildTreeNode(child, childrenMap))
                .collect(Collectors.toList());

        return PageDto.TreeNode.from(page, children);
    }

    // ── 페이지 단건 조회 ───────────────────────────────────────────────
    @Override
    public PageDto.Response getPage(Long pageId) {
        Page page = pageRepository.findActiveById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));
        return PageDto.Response.from(page);
    }

    // ── 페이지 수정 ────────────────────────────────────────────────────
    @Override
    @Transactional
    public PageDto.Response updatePage(Long pageId, PageDto.UpdateRequest request, Long memberId) {
        Page page = pageRepository.findActiveById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

        if (request.getTitle()     != null) page.setTitle(request.getTitle());
        if (request.getContent()   != null) page.setContent(request.getContent());
        if (request.getIconEmoji() != null) page.setIconEmoji(request.getIconEmoji());
        if (request.getIsPublic()  != null) page.setIsPublic(request.getIsPublic());

        return PageDto.Response.from(page);
    }

    // ── 페이지 소프트 삭제 ─────────────────────────────────────────────
    @Override
    @Transactional
    public void deletePage(Long pageId, Long memberId) {
        Page page = pageRepository.findActiveById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

        // 생성자 또는 워크스페이스 owner만 삭제 가능
        boolean isCreator = page.getCreatedBy().getMemberId().equals(memberId);
        boolean isOwner   = page.getWorkspace().getOwner().getMemberId().equals(memberId);
        if (!isCreator && !isOwner) {
            throw new SecurityException("페이지 삭제 권한이 없습니다.");
        }

        page.setDeletedAt(LocalDateTime.now());
    }

    // ── 채팅 메시지 → 페이지 블록 추가 (북마크) ─────────────────────────
    @Override
    @Transactional
    public void bookmarkMessageToPage(Long pageId, PageDto.BookmarkRequest request, Long memberId) {
        Page page = pageRepository.findActiveById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        // 중복 북마크 방지
        if (bookmarkRepository.existsByMessage_MessageIdAndPage_PageIdAndMember_MemberId(
                request.getMessageId(), pageId, memberId)) {
            throw new IllegalStateException("이미 북마크된 메시지입니다.");
        }

        // ✨ [수정됨] 페이지 content(JSON)를 객체/배열 여부에 따라 안전하게 파싱 후 추가
        try {
            String currentContent = page.getContent() != null && !page.getContent().isBlank() ? page.getContent().trim() : "{\"type\":\"doc\",\"content\":[]}";
            
            JsonNode rootNode = objectMapper.readTree(currentContent);
            
            List<Map<String, Object>> blocks = new ArrayList<>();
            Map<String, Object> docObject = null;
            
            if (rootNode.isObject()) {
                // Tiptap 기본 형식: { "type": "doc", "content": [...] }
                docObject = objectMapper.convertValue(rootNode, new TypeReference<Map<String, Object>>() {});
                if (docObject.containsKey("content") && docObject.get("content") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> existingBlocks = (List<Map<String, Object>>) docObject.get("content");
                    blocks.addAll(existingBlocks);
                }
            } else if (rootNode.isArray()) {
                // 배열 형식: [...] 
                blocks.addAll(objectMapper.convertValue(rootNode, new TypeReference<List<Map<String, Object>>>() {}));
            }

            // 구분선 블록
            blocks.add(Map.of("type", "horizontalRule"));

            // 출처 표시 (작은 텍스트)
            blocks.add(Map.of(
                "type", "paragraph",
                "attrs", Map.of("textAlign", "left"),
                "content", List.of(Map.of(
                    "type", "text",
                    "marks", List.of(Map.of("type", "italic")),
                    "text", "📌 채팅에서 가져온 내용"
                ))
            ));

            // 실제 메시지 내용 블록
            blocks.add(Map.of(
                "type", "blockquote",
                "content", List.of(Map.of(
                    "type", "paragraph",
                    "content", List.of(Map.of(
                        "type", "text",
                        "text", request.getContent()
                    ))
                ))
            ));

            String newContent;
            if (docObject != null) {
                // Object 형태였으면 content 부분만 교체해서 덮어쓰기
                docObject.put("content", blocks);
                newContent = objectMapper.writeValueAsString(docObject);
            } else {
                // Array 형태였으면 배열 자체를 덮어쓰기
                newContent = objectMapper.writeValueAsString(blocks);
            }

            page.setContent(newContent);

        } catch (Exception e) {
            throw new RuntimeException("페이지 내용 업데이트 중 오류가 발생했습니다.", e);
        }

        // ── bookmarks 테이블에 기록 ───────────────────────────────────
        bookmarkRepository.save(Bookmark.builder()
                .message(message)
                .page(page)
                .member(member)
                .build());
    }

    // ── 재귀 깊이 계산 헬퍼 ───────────────────────────────────────────
    private int getDepth(Page page) {
        int depth = 0;
        Page current = page;
        while (current.getParent() != null) {
            depth++;
            current = current.getParent();
            if (depth >= 3) break;
        }
        return depth;
    }
}