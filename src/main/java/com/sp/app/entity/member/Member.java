package com.sp.app.entity.member;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
 * member1 테이블 매핑
 * 로그인 계정 정보 (인증/인가 중심)
 */
@Entity
@Table(name = "member1")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "member_seq")
    @SequenceGenerator(name = "member_seq", sequenceName = "member_seq", allocationSize = 1)
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "login_id", unique = true, length = 100)
    private String loginId;

    @Column(name = "password", length = 100)
    private String password;

    /** SNS 제공자 (KAKAO / NAVER / GOOGLE) */
    @Column(name = "sns_provider", length = 50)
    private String snsProvider;

    /** SNS 고유 식별자 */
    @Column(name = "sns_id", length = 100)
    private String snsId;

    /** 회원 역할: 1=일반, 31=강사, 51=사원, 99=관리자, 0=비회원, 50=퇴사 */
    @Column(name = "userLevel", nullable = false)
    @Builder.Default
    private Integer userLevel = 1;

    /** 로그인 가능 여부: 1=가능, 0=불가 */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Integer enabled = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "failure_cnt")
    @Builder.Default
    private Integer failureCnt = 0;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** member2 와 1:1 */
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberDetail memberDetail;

    /** 권한 (1:1 — login_id PK) */
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberAuthority memberAuthority;

    /** 리프레시 토큰 (1:1 — login_id PK) */
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private RefreshToken refreshToken;

    // ──────────────────────────────────────────────
    // 생명주기 콜백
    // ──────────────────────────────────────────────

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updateAt  = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}
