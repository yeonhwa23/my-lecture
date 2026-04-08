package com.sp.app.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.FileManager;
import com.sp.app.controller.ChatController.ChatMessageResponse;
import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;
import com.sp.app.entity.member.Member;
import com.sp.app.entity.workspace.Channel;
import com.sp.app.entity.workspace.Message;
import com.sp.app.entity.workspace.MessageAttachment;
import com.sp.app.entity.workspace.Workspace;
import com.sp.app.entity.workspace.WorkspaceMember;
import com.sp.app.repository.ChannelRepository;
import com.sp.app.repository.MemberRepository;
import com.sp.app.repository.MessageAttachmentRepository;
import com.sp.app.repository.MessageRepository;
import com.sp.app.repository.WorkspaceMemberRepository;
import com.sp.app.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository           workspaceRepository;
    private final WorkspaceMemberRepository     workspaceMemberRepository;
    private final MemberRepository              memberRepository;
    private final ChannelRepository             channelRepository;
    private final MessageRepository             messageRepository;
    private final MessageAttachmentRepository   messageAttachmentRepository;
    @Autowired
    private FileManager fileManager;

    // ── 워크스페이스 생성 ──────────────────────────────────────────────
    @Override
    @Transactional
    public WorkspaceDto.Response createWorkspace(WorkspaceDto.CreateRequest request, Long loginMemberId) {

        if (workspaceRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("이미 사용 중인 슬러그입니다: " + request.getSlug());
        }

        Member owner = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Workspace workspace = Workspace.builder()
                .wsName(request.getWsName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .bannerUrl(request.getBannerUrl())
                .owner(owner)
                .build();
        workspaceRepository.save(workspace);

        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspaceId(workspace.getWorkspaceId())
                .memberId(loginMemberId)
                .workspace(workspace)
                .member(owner)
                .wsRole("owner")
                .build();
        workspaceMemberRepository.save(workspaceMember);

        return WorkspaceDto.Response.from(workspace);
    }

    // ── 워크스페이스 설정 업데이트 ────────────────────────────────────
    @Override
    @Transactional
    public void updateWorkspaceSettings(Long workspaceId, Long requestMemberId, String wsName, String description,
            String iconString, MultipartFile iconFile, MultipartFile bannerFile, boolean removeBanner, String pathname) throws Exception {

        Workspace ws = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 워크스페이스입니다."));

        if (!ws.getOwner().getMemberId().equals(requestMemberId)) {
            throw new SecurityException("설정은 방장만 변경할 수 있습니다.");
        }

        ws.setWsName(wsName);
        ws.setDescription(description);

        if (iconFile != null && !iconFile.isEmpty()) {
            String saveFilename = fileManager.doFileUpload(iconFile, pathname);
            ws.setIconUrl("/uploads/workspace/" + saveFilename);
        } else if (iconString != null && !iconString.trim().isEmpty()) {
            ws.setIconUrl(iconString);
        } else if (iconString != null && iconString.trim().isEmpty()) {
            ws.setIconUrl(null);
        }

        if (bannerFile != null && !bannerFile.isEmpty()) {
            String saveFilename = fileManager.doFileUpload(bannerFile, pathname);
            ws.setBannerUrl("/uploads/workspace/" + saveFilename);
        } else if (removeBanner) {
            ws.setBannerUrl(null);
        }
    }

    // ── 내 워크스페이스 목록 ──────────────────────────────────────────
    @Override
    public List<WorkspaceDto.Response> getMyWorkspaces(Long loginMemberId) {
        return workspaceRepository.findAllByMemberId(loginMemberId)
                .stream()
                .map(WorkspaceDto.Response::from)
                .toList();
    }

    @Override
    public WorkspaceDto.SlugCheckResponse checkSlug(String slug) {
        boolean available = !workspaceRepository.existsBySlug(slug);
        return new WorkspaceDto.SlugCheckResponse(available, slug);
    }

    // ── 슬러그로 참여 ─────────────────────────────────────────────────
    @Override
    @Transactional
    public WorkspaceDto.Response joinBySlug(String slug, Long loginMemberId) {

        Workspace workspace = workspaceRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 초대 링크입니다."));

        if (workspaceMemberRepository.existsByWorkspaceIdAndMemberId(
                workspace.getWorkspaceId(), loginMemberId)) {
            return WorkspaceDto.Response.from(workspace);
        }

        Member member = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        WorkspaceMember wm = WorkspaceMember.builder()
                .workspaceId(workspace.getWorkspaceId())
                .memberId(loginMemberId)
                .workspace(workspace)
                .member(member)
                .wsRole("member")
                .build();
        workspaceMemberRepository.save(wm);

        return WorkspaceDto.Response.from(workspace);
    }

    @Override
    public List<WorkspaceMemberDto.Response> getMembers(Long workspaceId) {
        return workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(WorkspaceMemberDto.Response::from)
                .toList();
    }

    @Override
    @Transactional
    public void kickMember(Long workspaceId, Long targetMemberId, Long requestMemberId) {

        WorkspaceMember requester = workspaceMemberRepository
                .findByWorkspaceIdAndMemberId(workspaceId, requestMemberId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스 접근 권한이 없습니다."));

        if ("member".equals(requester.getWsRole())) {
            throw new SecurityException("강퇴 권한이 없습니다.");
        }

        WorkspaceMember target = workspaceMemberRepository
                .findByWorkspaceIdAndMemberId(workspaceId, targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("대상 멤버가 존재하지 않습니다."));

        if ("owner".equals(target.getWsRole())) {
            throw new IllegalArgumentException("워크스페이스 소유자는 강퇴할 수 없습니다.");
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndMemberId(workspaceId, targetMemberId);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long workspaceId,
                                 WorkspaceMemberDto.RoleUpdateRequest request,
                                 Long requestMemberId) {

        WorkspaceMember requester = workspaceMemberRepository
                .findByWorkspaceIdAndMemberId(workspaceId, requestMemberId)
                .orElseThrow(() -> new IllegalArgumentException("워크스페이스 접근 권한이 없습니다."));

        if (!"owner".equals(requester.getWsRole())) {
            throw new SecurityException("역할 변경 권한이 없습니다. (owner만 가능)");
        }

        WorkspaceMember target = workspaceMemberRepository
                .findByWorkspaceIdAndMemberId(workspaceId, request.getTargetMemberId())
                .orElseThrow(() -> new IllegalArgumentException("대상 멤버가 존재하지 않습니다."));

        target.setWsRole(request.getNewRole());
    }

    // ── 채팅 메시지 저장 (첨부파일 포함) ─────────────────────────────
    @Override
    @Transactional
    public Long saveChatMessage(Long channelId, Long memberId, String content,
                                String imageUrl, String fileUrl, String fileName) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 파일만 보낼 때 content가 null → ORA-01400 방지
        String safeContent = (content != null && !content.isBlank()) ? content : " ";


        Message message = Message.builder()
                .channel(channel)
                .member(member)
                .content(safeContent)
                .isBot(0)
                .build();

        messageRepository.save(message);

        // 이미지 첨부
        if (imageUrl != null && !imageUrl.isBlank()) {
            String guessedName = imageUrl.contains("/") 
                ? imageUrl.substring(imageUrl.lastIndexOf('/') + 1) 
                : "image";
            messageAttachmentRepository.save(MessageAttachment.builder()
                    .message(message)
                    .fileUrl(imageUrl)
                    .fileName(guessedName)
                    .fileSize(0L)
                    .mimeType("image/*")
                    .build());
        }

        // 일반 파일 첨부
        if (fileUrl != null && !fileUrl.isBlank()) {
            messageAttachmentRepository.save(MessageAttachment.builder()
                    .message(message)
                    .fileUrl(fileUrl)
                    .fileName(fileName != null ? fileName : "file")
                    .fileSize(0L)
                    .mimeType("application/octet-stream")
                    .build());
        }

        return message.getMessageId();
    }

    // ── 채팅 히스토리 조회 ────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(Long channelId) {

        PageRequest pageRequest = PageRequest.of(0, 50);
        Slice<Message> messageSlice = messageRepository.findByChannelCursor(
                channelId, LocalDateTime.now(), pageRequest);

        List<ChatMessageResponse> responseList = messageSlice.getContent().stream()
                .map(msg -> {
                    ChatMessageResponse response = new ChatMessageResponse();
                    response.setMessageId(msg.getMessageId());
                    response.setChannelId(channelId);

                    if (msg.getMember() != null) {
                        response.setMemberId(msg.getMember().getMemberId());
                        response.setSenderName(msg.getMember().getLoginId());
                    } else {
                        response.setMemberId(-1L);
                        response.setSenderName("알 수 없는 사용자");
                    }

                    response.setContent(msg.getContent());
                    response.setSendTime(msg.getCreatedAt().toString());

                    // 첨부파일 정보 포함 (첫 번째 첨부파일 기준)
                    if (msg.getAttachments() != null && !msg.getAttachments().isEmpty()) {
                        MessageAttachment att = msg.getAttachments().get(0);
                        String mime = att.getMimeType();
                        if (mime != null && mime.startsWith("image")) {
                            response.setImageUrl(att.getFileUrl());
                        } else {
                            response.setFileUrl(att.getFileUrl());
                            response.setFileName(att.getFileName());
                        }
                    }

                    return response;
                })
                .collect(Collectors.toList());

        Collections.reverse(responseList);
        return responseList;
    }
}