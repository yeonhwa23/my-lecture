package com.sp.app.entity.member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * memberAuthority 테이블 매핑
 * 회원 권한 정보 (login_id 를 PK 겸 FK로 사용)
 *
 * 권한 예시: ADMIN, EMP, INSTRUCTOR, USER, EX_EMP
 */
@Entity
@Table(name = "memberAuthority")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAuthority {

    /**
     * PK = login_id (문자열)
     * member1.login_id 를 FK 로도 사용
     */
    @Id
    @Column(name = "login_id", length = 100)
    private String loginId;

    @Column(name = "authority", nullable = false, length = 100)
    private String authority;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /**
     * member1.login_id → 연결
     * @MapsId 대신 insertable/updatable=false 로 공유
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", referencedColumnName = "login_id", insertable = false, updatable = false)
    private Member member;
}
