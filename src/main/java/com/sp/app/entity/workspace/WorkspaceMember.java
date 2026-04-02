package com.sp.app.entity.workspace;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.sp.app.entity.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * workspace_members 테이블 매핑
 * 복합 PK: workspace_id + member_id
 */
@Entity
@Table(name = "workspace_members")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WorkspaceMember.WorkspaceMemberId.class)
public class WorkspaceMember {

    @Id
    @Column(name = "workspace_id")
    private Long workspaceId;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    /** 워크스페이스 내 역할: owner / admin / member */
    @Column(name = "ws_role", length = 20)
    @Builder.Default
    private String wsRole = "member";

    @Column(name = "joined_at", updatable = false)
    private LocalDateTime joinedAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", insertable = false, updatable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private Member member;

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    // ──────────────────────────────────────────────
    // 복합 PK 클래스
    // ──────────────────────────────────────────────

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class WorkspaceMemberId implements Serializable {
        private Long workspaceId;
        private Long memberId;
    }
}
