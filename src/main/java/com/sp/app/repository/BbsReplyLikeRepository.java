package com.sp.app.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.board.BbsReplyLike;

public interface BbsReplyLikeRepository extends JpaRepository<BbsReplyLike, BbsReplyLike.BbsReplyLikeId> {

    Optional<BbsReplyLike> findByReplyNumAndMemberId(Long replyNum, Long memberId);

    @Query("SELECT COUNT(r) FROM BbsReplyLike r WHERE r.replyNum = :replyNum AND r.replyLike = :replyLike")
    long countByReplyNumAndReplyLikeValue(@Param("replyNum") Long replyNum, @Param("replyLike") Integer replyLike);

    boolean existsByReplyNumAndMemberId(Long replyNum, Long memberId);

    void deleteByReplyNumAndMemberId(Long replyNum, Long memberId);
}