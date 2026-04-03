// src/main/java/com/sp/app/domain/dto/WorkspaceDto.java
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
        private String description; 
        private String bannerUrl;
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
        private String        description; 
        private String        bannerUrl;   
        private Long          ownerMemberId;
        private LocalDateTime createdAt;
        private LocalDateTime updateAt;   

        // 엔티티를 통신용 DTO로 변환하는 메서드
        public static Response from(Workspace ws) {
            return Response.builder()
                    .workspaceId(ws.getWorkspaceId())
                    .wsName(ws.getWsName())
                    .slug(ws.getSlug())
                    .iconUrl(ws.getIconUrl())
                    .description(ws.getDescription())
                    .bannerUrl(ws.getBannerUrl())
                    .ownerMemberId(ws.getOwner() != null ? ws.getOwner().getMemberId() : null)
                    .createdAt(ws.getCreatedAt())
                    .updateAt(ws.getUpdateAt())
                    .build();
        }
    }

    /** 슬러그 중복 확인 응답  */
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlugCheckResponse {
        private boolean available;
        private String  slug;
    }
}