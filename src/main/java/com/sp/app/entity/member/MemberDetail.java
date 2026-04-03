package com.sp.app.entity.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * member2 테이블 매핑
 * 실제 DB 컬럼 기준으로 작성
 *
 * 실제 컬럼 목록:
 * MEMBER_ID, NICKNAME, PROFILE_IMAGE, BIO,
 * PHONE_NUMBER, PREFERRED_REGION, UPDATED_AT, LAST_LOGIN_AT, EQUIPPED_BADGE_ID
 */
@Entity
@Table(name = "member2")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDetail {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "birth")
    private java.time.LocalDate birth;

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

    @Column(name = "receive_email")
    private Integer receiveEmail;

    @Column(name = "ipAddr", length = 100)
    private String ipAddr;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    private Member member;
}