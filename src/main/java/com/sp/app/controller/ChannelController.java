package com.sp.app.controller;

import com.sp.app.entity.workspace.Channel;
import com.sp.app.repository.ChannelRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelRepository channelRepository;

    // 1. 채널 목록 불러오기
    @GetMapping
    public List<ChannelDto> getChannels(@PathVariable("workspaceId") Long workspaceId) {
        // DB에서 해당 워크스페이스의 활성화된 채널(isArchived=0)을 가져옵니다.
        List<Channel> channels = channelRepository.findByWorkspace_WorkspaceIdAndIsArchivedOrderByChNameAsc(workspaceId, 0);
        
        // 프론트엔드에 필요한 데이터만 DTO로 변환해서 전달 (무한루프 방지)
        return channels.stream().map(c -> {
            ChannelDto dto = new ChannelDto();
            dto.setChannelId(c.getChannelId());
            dto.setChName(c.getChName());
            return dto;
        }).collect(Collectors.toList());
    }

    // 2. 새 채널 생성하기
    @PostMapping
    public void createChannel(@PathVariable("workspaceId") Long workspaceId, 
                              @RequestBody ChannelRequest req) {
        // 아까 Repository에 만든 네이티브 쿼리 실행!
        channelRepository.insertChannel(workspaceId, req.getChName(), req.getMemberId());
    }

    // --- 통신용 DTO ---
    @Data
    public static class ChannelDto {
        private Long channelId;
        private String chName;
    }

    @Data
    public static class ChannelRequest {
        private String chName;
        private Long memberId;
    }
}