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
import jakarta.persistence.Lob;
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
 * messages 테이블 매핑
 * - parent_id 로 스레드(자기참조) 구현
 * - member_id nullable → AI 봇 메시지 허용
 * - deleted_at soft-delete
 */
@Entity
@Table(name = "messages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messages_seq")
    @SequenceGenerator(name = "messages_seq", sequenceName = "messages_seq", allocationSize = 1)
    @Column(name = "message_id")
    private Long messageId;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    /** 0=사람, 1=AI 봇 */
    @Column(name = "is_bot")
    @Builder.Default
    private Integer isBot = 0;

    /** 사용한 AI 모델 (ex: gemini-pro, claude-3) */
    @Column(name = "ai_model", length = 50)
    private String aiModel;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    /** soft-delete: 삭제 시각 기록, null=미삭제 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 소속 채널 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    /**
     * 발신자 (nullable: AI 봇 메시지는 null)
     * ON DELETE SET NULL → orphanRemoval 사용 안 함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * 스레드 부모 메시지 (자기참조)
     * null = 최상위 메시지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Message parent;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
