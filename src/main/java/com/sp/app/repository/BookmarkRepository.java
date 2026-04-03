package com.sp.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.workspace.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    boolean existsByMessage_MessageIdAndPage_PageIdAndMember_MemberId(
            Long messageId, Long pageId, Long memberId);
}
