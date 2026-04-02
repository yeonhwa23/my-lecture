package com.sp.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.entity.workspace.MessageAttachment;

/**
 * message_attachments 테이블
 */
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

    /** 특정 메시지의 첨부파일 목록 */
    List<MessageAttachment> findByMessage_MessageId(Long messageId);

    /** 특정 MIME 타입 필터 (ex: "image/png") */
    List<MessageAttachment> findByMessage_MessageIdAndMimeTypeStartingWith(
            Long messageId, String mimeTypePrefix);
}
