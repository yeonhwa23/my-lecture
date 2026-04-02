package com.sp.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.member.MemberDetail;

/**
 * member2 테이블
 */
public interface MemberDetailRepository extends JpaRepository<MemberDetail, Long> {

    Optional<MemberDetail> findByEmail(String email);

    boolean existsByEmail(String email);
}
