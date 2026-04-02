package com.sp.app.repository;


import com.sp.app.entity.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // memberDetail, memberAuthority 즉시 로딩 (N+1 방지)
    @Query("SELECT m FROM Member m " +
           "LEFT JOIN FETCH m.memberDetail " +
           "LEFT JOIN FETCH m.memberAuthority " +
           "WHERE m.loginId = :loginId")
    Optional<Member> findByLoginId(@Param("loginId") String loginId);
}
