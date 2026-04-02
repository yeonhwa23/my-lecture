package com.sp.app.domain.dto;

import com.sp.app.entity.workspace.Workspace;
import lombok.*;
import java.time.LocalDateTime;

public class WorkspaceDto {

    /** 워크스페이스 생성 요청 */
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String wsName;
        private String slug;
        private String iconUrl;
    }

    /** 슬러그로 입장 요청 */
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinRequest {
        private String slug;
    }

    /** 응답 */
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long          workspaceId;
        private String        wsName;
        private String        slug;
        private String        iconUrl;
        private Long          ownerMemberId;
        private LocalDateTime createdAt;

        public static Response from(Workspace ws) {
            return Response.builder()
                    .workspaceId(ws.getWorkspaceId())
                    .wsName(ws.getWsName())
                    .slug(ws.getSlug())
                    .iconUrl(ws.getIconUrl())
                    .ownerMemberId(ws.getOwner() != null ? ws.getOwner().getMemberId() : null)
                    .createdAt(ws.getCreatedAt())
                    .build();
        }
    }

    /** 슬러그 중복 확인 응답 */
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlugCheckResponse {
        private boolean available;
        private String  slug;
    }
}