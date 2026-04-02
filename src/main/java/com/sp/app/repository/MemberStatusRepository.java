package com.sp.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.member.MemberStatus;

/**
 * memberStatus 테이블
 */
public interface MemberStatusRepository extends JpaRepository<MemberStatus, Long> {

    /** 특정 회원의 상태 이력 최신순 */
    List<MemberStatus> findByMember_MemberIdOrderByRegDateDesc(Long memberId);

    /** 특정 회원의 가장 최근 상태 1건 */
    @Query("SELECT ms FROM MemberStatus ms WHERE ms.member.memberId = :memberId ORDER BY ms.regDate DESC")
    Optional<MemberStatus> findLatestByMemberId(@Param("memberId") Long memberId);
}
