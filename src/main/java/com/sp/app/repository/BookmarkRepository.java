package com.sp.app.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.entity.workspace.Bookmark;

/**
 * bookmarks 테이블
 */
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 특정 회원의 북마크 목록 (최신순)
     */
    List<Bookmark> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * 특정 페이지에 아카이빙된 메시지 목록
     */
    List<Bookmark> findByPage_PageIdOrderByCreatedAtAsc(Long pageId);

    /**
     * 중복 여부 확인 (UNIQUE 제약 보조)
     */
    boolean existsByMessage_MessageIdAndPage_PageIdAndMember_MemberId(
            Long messageId, Long pageId, Long memberId);

    /**
     * 특정 북마크 단건 조회
     */
    Optional<Bookmark> findByMessage_MessageIdAndPage_PageIdAndMember_MemberId(
            Long messageId, Long pageId, Long memberId);

    /**
     * 특정 메시지가 북마크된 횟수
     */
    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.message.messageId = :messageId")
    long countByMessageId(@Param("messageId") Long messageId);

    void deleteByMessage_MessageIdAndMember_MemberId(Long messageId, Long memberId);
}
