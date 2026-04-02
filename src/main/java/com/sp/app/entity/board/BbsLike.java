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

// =====================================================================
// BbsLike — 게시글 좋아요 (복합 PK: num + member_id)
// =====================================================================

@Entity
@Table(name = "bbsLike")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(BbsLike.BbsLikeId.class)
public class BbsLike {

    @Id
    @Column(name = "num")
    private Long num;

    @Id
    @Column(name = "member_id")
    private Long memberId;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "num", insertable = false, updatable = false)
    private Bbs bbs;

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
    public static class BbsLikeId implements Serializable {
        private Long num;
        private Long memberId;
    }
}