package com.sp.app.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Channel;

/**
 * channels 테이블
 */
public interface ChannelRepository extends JpaRepository<Channel, Long> {

    /** 워크스페이스의 활성 채널 목록 (아카이브 제외) */
    List<Channel> findByWorkspace_WorkspaceIdAndIsArchivedOrderByChNameAsc(
            Long workspaceId, Integer isArchived);

    /** 특정 회원이 참가한 채널 목록 */
    @Query("""
        SELECT c FROM Channel c
        JOIN ChannelMember cm ON cm.channelId = c.channelId
        WHERE cm.memberId = :memberId
          AND c.workspace.workspaceId = :workspaceId
          AND c.isArchived = 0
        ORDER BY c.chName ASC
    """)
    List<Channel> findJoinedChannels(@Param("workspaceId") Long workspaceId,
                                     @Param("memberId") Long memberId);
}
