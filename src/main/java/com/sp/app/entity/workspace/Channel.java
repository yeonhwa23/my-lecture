package com.sp.app.entity.workspace;

import java.time.LocalDateTime;

import com.sp.app.entity.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * channels 테이블 매핑
 * 워크스페이스 안의 채팅 채널 (Slack 채널과 동일 개념)
 */
@Entity
@Table(name = "channels")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channels_seq")
    @SequenceGenerator(name = "channels_seq", sequenceName = "channels_seq", allocationSize = 1)
    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "ch_name", nullable = false, length = 100)
    private String chName;

    @Column(name = "description", length = 1000)
    private String description;

    /** 0=공개, 1=비공개 */
    @Column(name = "is_private")
    @Builder.Default
    private Integer isPrivate = 0;

    /** 0=활성, 1=아카이브 */
    @Column(name = "is_archived")
    @Builder.Default
    private Integer isArchived = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 소속 워크스페이스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /** 채널 생성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
