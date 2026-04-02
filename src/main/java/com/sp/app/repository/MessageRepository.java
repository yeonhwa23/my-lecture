package com.sp.app.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Message;

/**
 * messages 테이블
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 채널 메시지 최신순 커서 페이징 (무한 스크롤)
     * deletedAt IS NULL → soft-delete 제외
     * parent IS NULL    → 스레드 부모 메시지만 조회 (스레드 답글은 별도 조회)
     */
    @Query("""
        SELECT m FROM Message m
        WHERE m.channel.channelId = :channelId
          AND m.parent IS NULL
          AND m.deletedAt IS NULL
          AND m.createdAt < :cursor
        ORDER BY m.createdAt DESC
    """)
    Slice<Message> findByChannelCursor(@Param("channelId") Long channelId,
                                       @Param("cursor") LocalDateTime cursor,
                                       Pageable pageable);

    /** 특정 메시지의 스레드 답글 오래된 순 */
    List<Message> findByParent_MessageIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long parentId);

    /** 안읽음 수: last_read_at 이후 메시지 수 */
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.channel.channelId = :channelId
          AND m.createdAt > :since
          AND m.deletedAt IS NULL
    """)
    long countUnread(@Param("channelId") Long channelId,
                     @Param("since") LocalDateTime since);

    /** AI 봇 메시지 필터 조회 */
    List<Message> findByChannel_ChannelIdAndIsBotOrderByCreatedAtDesc(
            Long channelId, Integer isBot, Pageable pageable);
}
