package com.sp.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.member.MemberAuthority;

/**
 * memberAuthority 테이블
 * PK = login_id (String)
 */
public interface MemberAuthorityRepository extends JpaRepository<MemberAuthority, String> {

    Optional<MemberAuthority> findByLoginId(String loginId);
}
