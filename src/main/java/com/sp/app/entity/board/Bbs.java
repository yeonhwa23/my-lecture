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
 * bbs 테이블 매핑 — 게시판 본문
 */
@Entity
@Table(name = "bbs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bbs {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bbs_seq")
    @SequenceGenerator(name = "bbs_seq", sequenceName = "bbs_seq", allocationSize = 1)
    @Column(name = "num")
    private Long num;

    @Column(name = "subject", nullable = false, length = 250)
    private String subject;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "hitCount")
    @Builder.Default
    private Integer hitCount = 0;

    @Column(name = "reg_date", updatable = false)
    private LocalDateTime regDate;

    @Column(name = "saveFilename", length = 500)
    private String saveFilename;

    @Column(name = "originalFilename", length = 500)
    private String originalFilename;

    /** 0=정상, 1=차단 */
    @Column(name = "block")
    @Builder.Default
    private Integer block = 0;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** bbs.member_id → member2 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberDetail memberDetail;

    @PrePersist
    public void prePersist() {
        this.regDate = LocalDateTime.now();
    }
}
