package com.sp.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.board.Bbs;

/**
 * bbs 테이블
 */
public interface BbsRepository extends JpaRepository<Bbs, Long> {

    /** 차단되지 않은 게시글 최신순 페이징 */
    Page<Bbs> findByBlockOrderByRegDateDesc(Integer block, Pageable pageable);

    /** 제목 검색 (차단 제외) */
    Page<Bbs> findBySubjectContainingAndBlock(String keyword, Integer block, Pageable pageable);

    /** 조회수 +1 */
    @Modifying
    @Query("UPDATE Bbs b SET b.hitCount = b.hitCount + 1 WHERE b.num = :num")
    void incrementHitCount(@Param("num") Long num);
}
