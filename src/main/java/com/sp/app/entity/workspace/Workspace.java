package com.sp.app.entity.workspace;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sp.app.entity.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * workspaces 테이블 매핑
 * Slack 의 워크스페이스에 해당
 */
@Entity
@Table(name = "workspaces")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workspaces_seq")
    @SequenceGenerator(name = "workspaces_seq", sequenceName = "workspaces_seq", allocationSize = 1)
    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(name = "ws_name", nullable = false, length = 100)
    private String wsName;

    /** 초대 링크용 URL 슬러그 (unique) */
    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 워크스페이스 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    
    /** 워크스페이스 참여 멤버 목록 */
    @Builder.Default
    @OneToMany(mappedBy = "workspace")
    private List<WorkspaceMember> workspaceMembers = new ArrayList<>();
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
