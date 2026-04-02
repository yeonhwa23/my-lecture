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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * pages 테이블 매핑 — Notion 스타일 문서 페이지
 *
 * - parent_id 자기참조로 트리 구조 구현 (최대 3단계 권장)
 * - content : Tiptap 블록 에디터 JSON → CLOB 저장
 * - deleted_at soft-delete
 */
@Entity
@Table(name = "pages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pages_seq")
    @SequenceGenerator(name = "pages_seq", sequenceName = "pages_seq", allocationSize = 1)
    @Column(name = "page_id")
    private Long pageId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /**
     * Tiptap / ProseMirror 블록 JSON
     * ex) [{"type":"paragraph","content":[{"type":"text","text":"Hello"}]}]
     */
    @Lob
    @Column(name = "content")
    @Builder.Default
    private String content = "[]";

    /** 0=비공개(기본), 1=공개 */
    @Column(name = "is_public")
    @Builder.Default
    private Integer isPublic = 0;

    /** 페이지 아이콘 이모지 (ex: 📄 ✅) */
    @Column(name = "icon_emoji", length = 10)
    private String iconEmoji;

    /** 같은 부모 하위에서 정렬 순서 */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    /** soft-delete: 삭제 시각 기록, null=미삭제 */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 소속 워크스페이스 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    /** 페이지 생성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    /**
     * 부모 페이지 (자기참조)
     * null = 루트 페이지
     * ON DELETE 없음 → 부모 삭제 시 자식이 고아가 되지 않도록 서비스 레이어에서 처리
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Page parent;

    // ──────────────────────────────────────────────
    // 생명주기 콜백
    // ──────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
