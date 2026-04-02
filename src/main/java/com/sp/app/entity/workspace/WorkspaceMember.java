package com.sp.app.entity.workspace;

import com.sp.app.entity.member.Member;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "workspace_members")
@IdClass(WorkspaceMember.WorkspaceMemberId.class)   // ← @IdClass 방식 → findByWorkspaceId 바로 사용 가능
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceMember {

    // ── 복합 PK (내부 클래스) ──────────────────────────────────
    @Embeddable
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class WorkspaceMemberId implements Serializable {
        private Long workspaceId;
        private Long memberId;
    }

    // ── @IdClass 방식: PK 컬럼을 엔티티에 직접 선언 ──────────
    @Id
    @Column(name = "workspace_id")
    private Long workspaceId;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    // owner / admin / member
    @Column(name = "ws_role", length = 20)
    @Builder.Default
    private String wsRole = "member";

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    // ── 연관관계 (@ManyToOne, insertable=false updatable=false) ─
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", insertable = false, updatable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @PrePersist
    public void prePersist() {
        if (joinedAt == null) joinedAt = LocalDateTime.now();
        if (wsRole   == null) wsRole   = "member";
    }
}