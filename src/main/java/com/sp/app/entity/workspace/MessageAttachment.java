package com.sp.app.entity.workspace;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * message_attachments 테이블 매핑
 * 메시지에 첨부된 파일 정보
 */
@Entity
@Table(name = "message_attachments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_attachments_seq")
    @SequenceGenerator(name = "message_attachments_seq", sequenceName = "message_attachments_seq", allocationSize = 1)
    @Column(name = "attach_id")
    private Long attachId;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    /** 원본 파일명 */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    /** 파일 크기 (bytes) */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ──────────────────────────────────────────────
    // 연관관계
    // ──────────────────────────────────────────────

    /** 첨부된 메시지 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
