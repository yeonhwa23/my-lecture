package com.sp.app.entity.workspace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sp.app.entity.member.Member;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pages")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pages_seq_gen")
    @SequenceGenerator(name = "pages_seq_gen", sequenceName = "pages_seq", allocationSize = 1)
    @Column(name = "page_id")
    private Long pageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    // 부모 페이지 (셀프 조인, 최대 3단계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Page parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Page> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // Tiptap 블록 JSON → CLOB
    @Lob
    @Column(name = "content")
    @Builder.Default
    private String content = "[]";

    @Column(name = "is_public")
    @Builder.Default
    private Integer isPublic = 0;

    @Column(name = "icon_emoji", length = 10)
    private String iconEmoji;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
