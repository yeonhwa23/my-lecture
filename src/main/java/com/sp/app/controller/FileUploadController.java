package com.sp.app.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.common.FileManager;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileManager fileManager;

    // ✅ 상수로 직접 지정 (application.properties 설정 불필요)
    // 본인 환경에 맞게 수정하세요
    private static final String UPLOAD_PATH = "C:/uploads/chat";   // 실제 저장 경로
    private static final String URL_PREFIX  = "/uploads/chat";     // 브라우저 접근 URL 접두사

    /**
     * POST /api/upload
     * ChatView에서 첨부파일 전송 시 호출
     * multipart/form-data, 파라미터명: files
     * 응답: { "urls": ["/uploads/chat/파일명", ...] }
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, List<String>>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files) {

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            try {
                String savedName = fileManager.doFileUpload(file, UPLOAD_PATH);
                if (savedName != null) {
                    urls.add(URL_PREFIX + "/" + savedName);
                }
            } catch (Exception e) {
                throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
            }
        }

        return ResponseEntity.ok(Map.of("urls", urls));
    }
}