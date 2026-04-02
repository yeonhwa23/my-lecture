package com.sp.app.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Reaction;

/**
 * reactions 테이블
 * PK = Reaction.ReactionId (@EmbeddedId 복합키)
 */
public interface ReactionRepository extends JpaRepository<Reaction, Reaction.ReactionId> {

    /** 특정 메시지의 전체 리액션 */
    List<Reaction> findById_MessageId(Long messageId);

    /** 특정 메시지의 이모지별 카운트 */
    @Query("""
        SELECT r.id.emoji AS emoji, COUNT(r) AS cnt
        FROM Reaction r
        WHERE r.id.messageId = :messageId
        GROUP BY r.id.emoji
    """)
    List<Object[]> countByEmoji(@Param("messageId") Long messageId);

    /** 특정 회원이 이미 누른 이모지 여부 */
    boolean existsById_MessageIdAndId_MemberIdAndId_Emoji(
            Long messageId, Long memberId, String emoji);

    void deleteById_MessageIdAndId_MemberIdAndId_Emoji(
            Long messageId, Long memberId, String emoji);
}
