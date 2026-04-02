package com.sp.app.domain.dto;

import com.sp.app.entity.workspace.WorkspaceMember;
import lombok.*;
import java.time.LocalDateTime;

public class WorkspaceMemberDto {

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long          memberId;
        private String        loginId;
        private String        name;
        private String        profilePhoto;
        private String        wsRole;
        private LocalDateTime joinedAt;

        public static Response from(WorkspaceMember wm) {
            return Response.builder()
                    .memberId(wm.getMemberId())
                    .loginId(wm.getMember() != null ? wm.getMember().getLoginId() : null)
                    .wsRole(wm.getWsRole())
                    .joinedAt(wm.getJoinedAt())
                    .build();
        }
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleUpdateRequest {
        private Long   targetMemberId;
        private String newRole;  // "admin" 또는 "member"
    }
}