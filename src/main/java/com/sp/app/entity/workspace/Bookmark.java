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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * bookmarks 테이블 매핑
 * 채팅 메시지 → Notion 페이지로 아카이빙할 때 사용
 *
 * UNIQUE(message_id, page_id, member_id) → 동일 북마크 중복 방지
 */
@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_bookmarks",
        columnNames = {"message_id", "page_id", "member_id"}
    )
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookmarks_seq")
    @SequenceGenerator(name = "bookmarks_seq", sequenceName = "bookmarks_seq", allocationSize = 1)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 북마크한 채팅 메시지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    /** 아카이빙 대상 페이지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    /** 북마크를 만든 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
