package com.sp.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.board.BbsLike;

/**
 * bbsLike 테이블 — 게시글 좋아요
 * PK = BbsLike.BbsLikeId (복합키)
 */
public interface BbsLikeRepository extends JpaRepository<BbsLike, BbsLike.BbsLikeId> {

    /** 특정 게시글 좋아요 수 */
    long countByNum(Long num);

    /** 특정 회원이 이미 좋아요 눌렀는지 */
    boolean existsByNumAndMemberId(Long num, Long memberId);

    void deleteByNumAndMemberId(Long num, Long memberId);
}
