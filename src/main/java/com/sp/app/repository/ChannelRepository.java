package com.sp.app.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
    
    /** ✅ 채널 생성용 네이티브 쿼리 추가 (에러 방지용) */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO channels (channel_id, workspace_id, ch_name, description, is_archived, is_private, created_by, created_at) 
        VALUES (channels_seq.NEXTVAL, :workspaceId, :chName, '새로운 채널입니다.', 0, 0, :memberId, SYSDATE)
    """, nativeQuery = true)
    void insertChannel(@Param("workspaceId") Long workspaceId, 
                       @Param("chName") String chName, 
                       @Param("memberId") Long memberId);
}
