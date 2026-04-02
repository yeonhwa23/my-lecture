package com.sp.app.entity.workspace;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.sp.app.entity.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * reactions 테이블 매핑 — 이모지 리액션
 * 복합 PK: message_id + member_id + emoji  (@EmbeddedId 사용)
 * 동일 이모지 중복 방지 (DB PRIMARY KEY 로 보장)
 */
@Entity
@Table(name = "reactions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {

    @EmbeddedId
    private ReactionId id;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────
    // 연관관계 (@MapsId 로 복합 PK 의 각 컬럼 공유)
    // ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ──────────────────────────────────────────────
    // 복합 PK — @Embeddable
    // ──────────────────────────────────────────────

    @Embeddable
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ReactionId implements Serializable {

        @Column(name = "message_id")
        private Long messageId;

        @Column(name = "member_id")
        private Long memberId;

        /** 이모지 코드 (ex: :thumbsup:, :heart:) */
        @Column(name = "emoji", length = 20)
        private String emoji;
    }
}
