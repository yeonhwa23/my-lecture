package com.sp.app.entity.member;

import jakarta.persistence.*;
import lombok.*;

/**
 * refreshToken 테이블 매핑
 * PK = login_id (member1.login_id FK 공유)
 */
@Entity
@Table(name = "refreshToken")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @Column(name = "login_id", length = 100)
    private String loginId;

    @Column(name = "rt_value", nullable = false, length = 500)
    private String rtValue;

    /** Member.refreshToken 의 반대쪽 (FK = login_id) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "login_id", referencedColumnName = "login_id", insertable = false, updatable = false)
    private Member member;

    public void updateValue(String newValue) {
        this.rtValue = newValue;
    }
}
