package com.sp.app.service;

import com.sp.app.domain.dto.WorkspaceDto;
import com.sp.app.domain.dto.WorkspaceMemberDto;
import com.sp.app.entity.member.Member;
import com.sp.app.entity.workspace.Workspace;
import com.sp.app.entity.workspace.WorkspaceMember;
import com.sp.app.repository.MemberRepository;
import com.sp.app.repository.WorkspaceMemberRepository;
import com.sp.app.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository       workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final MemberRepository          memberRepository;

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
}