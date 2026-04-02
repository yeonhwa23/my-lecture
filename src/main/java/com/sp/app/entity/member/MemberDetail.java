package com.sp.app.entity.member;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * member2 테이블 매핑
 * 회원 상세 정보 (프로필, 주소 등)
 * member1 과 PK 공유 (@MapsId)
 */
@Entity
@Table(name = "member2")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDetail {

    /** member1 의 member_id 를 PK 로 공유 */
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "zip", length = 7)
    private String zip;

    @Column(name = "addr1", length = 100)
    private String addr1;

    @Column(name = "addr2", length = 100)
    private String addr2;

    @Column(name = "email", length = 100)
    private String email;

    /** 이메일 수신 여부: 1=수신, 0=거부 */
    @Column(name = "receive_email")
    @Builder.Default
    private Integer receiveEmail = 1;

    @Column(name = "ipAddr", length = 100)
    private String ipAddr;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /**
     * @MapsId : member2 의 PK = member1 의 PK
     * INSERT 시 member1 의 member_id 를 그대로 사용
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;
}
