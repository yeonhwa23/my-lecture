package com.sp.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.ChannelMember;

/**
 * channel_members 테이블
 * PK = ChannelMember.ChannelMemberId (복합키)
 */
public interface ChannelMemberRepository
        extends JpaRepository<ChannelMember, ChannelMember.ChannelMemberId> {

    List<ChannelMember> findByChannelId(Long channelId);

    Optional<ChannelMember> findByChannelIdAndMemberId(Long channelId, Long memberId);

    boolean existsByChannelIdAndMemberId(Long channelId, Long memberId);

    void deleteByChannelIdAndMemberId(Long channelId, Long memberId);

    /** 안읽음 뱃지: last_read_at 이후 메시지 수 계산용으로 lastReadAt 갱신 */
    @Modifying
    @Query("""
        UPDATE ChannelMember cm
        SET cm.lastReadAt = :readAt
        WHERE cm.channelId = :channelId AND cm.memberId = :memberId
    """)
    void updateLastReadAt(@Param("channelId") Long channelId,
                          @Param("memberId") Long memberId,
                          @Param("readAt") LocalDateTime readAt);
}
