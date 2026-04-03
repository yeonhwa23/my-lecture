package com.sp.app.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.controller.ChatController.ChatMessageResponse;
import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;
import com.sp.app.entity.member.Member;
import com.sp.app.entity.workspace.Channel;
import com.sp.app.entity.workspace.Message;
import com.sp.app.entity.workspace.Workspace;
import com.sp.app.entity.workspace.WorkspaceMember;
import com.sp.app.repository.ChannelRepository;
import com.sp.app.repository.MemberRepository;
import com.sp.app.repository.MessageRepository;
import com.sp.app.repository.WorkspaceMemberRepository;
import com.sp.app.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository       workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final MemberRepository          memberRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public WorkspaceDto.Response createWorkspace(WorkspaceDto.CreateRequest request, Long loginMemberId) {

        if (workspaceRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("이미 사용 중인 슬러그입니다: " + request.getSlug());
        }

        Member owner = memberRepository.findById(loginMemberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 1) workspaces INSERT
        Workspace workspace = Workspace.builder()
                .wsName(request.getWsName())
                .slug(request.getSlug())
                .iconUrl(request.getIconUrl())
                .owner(owner)
                .build();
        workspaceRepository.save(workspace);

        // 2) workspace_members INSERT (owner 권한)
        // @IdClass 방식: workspaceId/memberId 필드 직접 세팅
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
    
    @Override
    @Transactional
    public void saveChatMessage(Long channelId, Long memberId, String content) {
        // 1. 대문자가 아니라 소문자로 시작하는 객체 변수명(channelRepository)을 사용해야 합니다!
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new IllegalArgumentException("채널을 찾을 수 없습니다."));
            
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 2. Message 엔티티 생성
        Message message = Message.builder()
                .channel(channel)
                .member(member)
                .content(content)
                .isBot(0) // 일반 유저 메시지
                .build();

        // 3. 소문자 messageRepository 사용!
        messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistory(Long channelId) {
        
        PageRequest pageRequest = PageRequest.of(0, 50);
        Slice<Message> messageSlice = messageRepository.findByChannelCursor(channelId, LocalDateTime.now(), pageRequest);

        List<Message> messages = messageSlice.getContent();
        
        List<ChatMessageResponse> responseList = messages.stream().map(msg -> {
            ChatMessageResponse response = new ChatMessageResponse();
            response.setChannelId(channelId);
            
            if (msg.getMember() != null) {
                response.setMemberId(msg.getMember().getMemberId());
                // ✅ getUsername() 대신 존재하는 메서드인 getLoginId() 사용!
                // 만약 진짜 이름을 쓰고 싶다면 msg.getMember().getMemberDetail().getName() 으로 쓸 수 있습니다.
                response.setSenderName(msg.getMember().getLoginId()); 
            } else {
                response.setMemberId(-1L);
                response.setSenderName("알 수 없는 사용자");
            }
            response.setContent(msg.getContent());
            response.setSendTime(msg.getCreatedAt().toString());
            return response;
        }).collect(Collectors.toList());

        // 최신순을 오래된 순(위에서 아래로 읽게)으로 뒤집기
        Collections.reverse(responseList);

        return responseList;
    }
}