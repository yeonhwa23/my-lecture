package com.sp.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.board.BbsReply;

/**
 * bbsReply 테이블
 */
public interface BbsReplyRepository extends JpaRepository<BbsReply, Long> {

    /** 특정 게시글의 댓글 목록 (오래된 순) */
    List<BbsReply> findByBbs_NumAndBlockOrderByReplyNumAsc(Long num, Integer block);

    /** 대댓글 목록 (parentNum 기준) */
    List<BbsReply> findByParentNumOrderByReplyNumAsc(Long parentNum);

    /** 특정 게시글 댓글 수 */
    long countByBbs_NumAndBlock(Long num, Integer block);
}
