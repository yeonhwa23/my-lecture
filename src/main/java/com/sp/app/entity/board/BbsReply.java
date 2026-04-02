package com.sp.app.entity.board;

import java.time.LocalDateTime;

import com.sp.app.entity.member.MemberDetail;

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
 * bbsReply 테이블 매핑 — 게시글 댓글
 * parentNum 으로 대댓글 자기참조 구현
 */
@Entity
@Table(name = "bbsReply")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BbsReply {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bbsReply_seq")
    @SequenceGenerator(name = "bbsReply_seq", sequenceName = "bbsReply_seq", allocationSize = 1)
    @Column(name = "replyNum")
    private Long replyNum;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "reg_date", updatable = false)
    private LocalDateTime regDate;

    /** 대댓글 부모 번호 (null = 최상위 댓글) */
    @Column(name = "parentNum")
    private Long parentNum;

    /** 1=노출, 0=숨김 */
    @Column(name = "showReply")
    @Builder.Default
    private Integer showReply = 1;

    /** 0=정상, 1=차단 */
    @Column(name = "block")
    @Builder.Default
    private Integer block = 0;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "num", nullable = false)
    private Bbs bbs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberDetail memberDetail;

    @PrePersist
    public void prePersist() {
        this.regDate = LocalDateTime.now();
    }
}
