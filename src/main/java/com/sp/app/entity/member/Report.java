package com.sp.app.entity.member;

import java.time.LocalDateTime;

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
 * reports 테이블 매핑 — 신고 테이블
 */
@Entity
@Table(name = "reports")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reports_seq")
    @SequenceGenerator(name = "reports_seq", sequenceName = "reports_seq", allocationSize = 1)
    @Column(name = "num")
    private Long num;

    /** 대상 테이블명 */
    @Column(name = "target", nullable = false, length = 500)
    private String target;

    /** 대상 게시글의 PK */
    @Column(name = "target_num", nullable = false)
    private Long targetNum;

    /** 콘텐츠 타입 (posts / photo / reply / replyAnswer) */
    @Column(name = "content_type", nullable = false, length = 500)
    private String contentType;

    /** 콘텐츠 제목 */
    @Column(name = "content_title", nullable = false, length = 500)
    private String contentTitle;

    /** 신고 사유 코드: SPAM / ABUSE / PORNOGRAPHY / INAPPROPRIATE / COPYRIGHT / OTHER */
    @Column(name = "reason_code", nullable = false, length = 500)
    private String reasonCode;

    @Column(name = "reason_detail", length = 4000)
    private String reasonDetail;

    @Column(name = "report_date", updatable = false)
    private LocalDateTime reportDate;

    @Column(name = "report_ip", nullable = false, length = 100)
    private String reportIp;

    /** 처리 상태: 1=신고접수, 2=처리완료, 3=기각 */
    @Column(name = "report_status")
    @Builder.Default
    private Integer reportStatus = 1;

    @Column(name = "action_taken", length = 4000)
    private String actionTaken;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 신고자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    /** 처리 담당자 (관리자, nullable) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    private Member processor;

    @PrePersist
    public void prePersist() {
        this.reportDate = LocalDateTime.now();
    }
}
