package com.sp.app.entity.member;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * memberStatus 테이블 매핑
 * 회원 상태 이력 (정지, 탈퇴, 복구 등)
 */
@Entity
@Table(name = "memberStatus")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "memberStatus_seq")
    @SequenceGenerator(name = "memberStatus_seq", sequenceName = "memberStatus_seq", allocationSize = 1)
    @Column(name = "num")
    private Long num;

    /** 상태 코드 (도메인별 정의) */
    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "memo", nullable = false, length = 1000)
    private String memo;

    @Column(name = "reg_date", updatable = false)
    private LocalDateTime regDate;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 상태 대상 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 상태를 등록한 관리자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "register_id", nullable = false)
    private Member registeredBy;

    @PrePersist
    public void prePersist() {
        this.regDate = LocalDateTime.now();
    }
}
