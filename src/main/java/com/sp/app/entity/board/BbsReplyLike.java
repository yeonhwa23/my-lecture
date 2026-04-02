package com.sp.app.entity.board;

import java.io.Serializable;

import com.sp.app.entity.member.MemberDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * bbsReplyLike 테이블 매핑 — 댓글 좋아요/싫어요
 * 복합 PK: replyNum + member_id
 */
@Entity
@Table(name = "bbsReplyLike")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BbsReplyLike.BbsReplyLikeId.class)
public class BbsReplyLike {

    @Id
    @Column(name = "replyNum")
    private Long replyNum;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    /** 1=좋아요, 0=싫어요 */
    @Column(name = "replyLike", nullable = false)
    private Integer replyLike;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replyNum", insertable = false, updatable = false)
    private BbsReply bbsReply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    private MemberDetail memberDetail;

    // ──────────────────────────────────────────────
    // 복합 PK 클래스
    // ──────────────────────────────────────────────

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class BbsReplyLikeId implements Serializable {
        private Long replyNum;
        private Long memberId;
    }
}
