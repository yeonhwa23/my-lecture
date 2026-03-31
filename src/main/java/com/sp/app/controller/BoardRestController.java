package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.common.MyUtil;
import com.sp.app.common.StorageService;
import com.sp.app.domain.dto.BoardDto;
import com.sp.app.domain.dto.LoginUser;
import com.sp.app.exception.StorageException;
import com.sp.app.security.CustomUserDetails;
import com.sp.app.service.BoardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/board")
public class BoardRestController {
	private final BoardService boardService;
	private final StorageService storageService;
	private final MyUtil myUtil;
	
	@Value("${file.upload-root}/board")
	private String uploadPath;

	@GetMapping
	public ResponseEntity<?> handleList(@RequestParam(name = "pageNo", defaultValue = "1") int current_page,
			@RequestParam(name = "pageSize", defaultValue = "10") int size,
			@RequestParam(name = "schType", defaultValue = "all") String schType,
			@RequestParam(name = "kwd", defaultValue = "") String kwd) throws Exception {
		
		try {
			int totalPage = 0;
			int totalCount = 0;

			kwd = myUtil.decodeUrl(kwd);

			// 전체 페이지 수
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("schType", schType);
			map.put("kwd", kwd);
			
			totalCount = boardService.dataCount(map);
			if (totalCount != 0) {
				totalPage = totalCount / size + (totalCount % size > 0 ? 1 : 0);
			}
			
			// 다른 사람이 자료를 삭제하여 전체 페이지수가 변화 된 경우
			current_page = Math.min(current_page, totalPage);

			// 리스트에 출력할 데이터를 가져오기
			int offset = (current_page - 1) * size;
			if(offset < 0) offset = 0;

			map.put("offset", offset);
			map.put("size", size);

			List<BoardDto> list = boardService.listBoard(map);
			
			return ResponseEntity.ok(Map.of(
	                "list", list, 
	                "totalCount", totalCount,
	                "pageNo", current_page,
	                "totalPage", totalPage
	            ));			
		} catch (Exception e) {
			log.info("handleList : ", e);
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PostMapping
	public ResponseEntity<?> handleSave(BoardDto dto,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {

		try {
			LoginUser loginUser = userDetails.getLoginUser();
			
			dto.setMember_id(loginUser.getMember_id());
			
			boardService.insertBoard(dto, uploadPath);
			
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.info("handleSave : ", e);
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/{num}")
	public ResponseEntity<?> handleDetail(@PathVariable("num") long num,
			@RequestParam(name = "schType", defaultValue = "all") String schType,
			@RequestParam(name = "kwd", defaultValue = "") String kwd,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
		
		try {
			LoginUser loginUser = userDetails.getLoginUser();
			
			kwd = myUtil.decodeUrl(kwd);
			
			boardService.updateHitCount(num);

			// 해당 레코드 가져 오기
			BoardDto dto = Objects.requireNonNull(boardService.findById(num));
			
			dto.setName(myUtil.nameMasking(dto.getName()));
			
			// dto.setContent(myUtil.htmlSymbols(dto.getContent())); // 에디터로 처리
			// dto.setContent(myUtil.sanitize(dto.getContent()));
					// XSS 방지을 위한 새니타이즈 메소드(위험한 속성, 스크립트 제거)
			if(dto.getMember_id() == loginUser.getMember_id()) {
				dto.setCanDelete(true);
				dto.setCanEdit(true);
			}
			
			if(loginUser.getUserLevel() >= 51) {
				dto.setCanDelete(true);
			}
			
			// 이전 글, 다음 글
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("schType", schType);
			map.put("kwd", kwd);
			map.put("num", num);
			
			BoardDto prevDto = boardService.findByPrev(map);
			BoardDto nextDto = boardService.findByNext(map);

			// 게시글 좋아요 여부
			map.put("member_id", loginUser.getMember_id());
			boolean isUserLiked = boardService.isUserBoardLiked(map);
			
			Map<String, Object> model = new HashMap<>();
			model.put("dto", dto);
			model.put("prevDto", prevDto); // null 이어도 에러 발생 안 함
			model.put("nextDto", nextDto);
			model.put("isUserLiked", isUserLiked);			
			
			return ResponseEntity.ok(model);
			
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글이 존재하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@GetMapping("/{num}/edit")
	public ResponseEntity<?> handleDetailEdit(@PathVariable("num") long num) throws Exception {
		try {
			BoardDto dto = Objects.requireNonNull(boardService.findById(num));

			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@PutMapping("/{num}")
	public ResponseEntity<?> handleUpdate(@PathVariable("num") long num, BoardDto dto) throws Exception {
		try {
			dto.setNum(num);
			boardService.updateBoard(dto, uploadPath);

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/file/{num}")
	public ResponseEntity<?> handleDeleteFile(@PathVariable(name = "num") long num,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
		
		try {
			LoginUser loginUser = userDetails.getLoginUser();
			
			BoardDto dto = Objects.requireNonNull(boardService.findById(num));
			
			if (dto.getMember_id() != loginUser.getMember_id()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
			}
			
			if (dto.getSaveFilename() != null) {
				storageService.deleteFile(uploadPath, dto.getSaveFilename());
				
				dto.setSaveFilename("");
				dto.setOriginalFilename("");
				boardService.updateBoard(dto, uploadPath); // DB 테이블의 파일명 변경(삭제)
			}
			
			return ResponseEntity.ok().build();
			
		} catch (NullPointerException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("게시글이 존재하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/{num}")
	public ResponseEntity<?> handleDelete(@PathVariable("num") long num,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
		// 게시글 삭제
		try {
			LoginUser loginUser = userDetails.getLoginUser();
			
			boardService.deleteBoard(num, uploadPath, loginUser.getMember_id(), loginUser.getUserLevel());

			return ResponseEntity.ok().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@GetMapping("/download/{num}")
	public ResponseEntity<?> download(
			@PathVariable("num") long num,
			@AuthenticationPrincipal UserDetails userDetails) throws Exception {
		
		try {
			BoardDto dto = Objects.requireNonNull(boardService.findById(num));

			return storageService.downloadFile(uploadPath, dto.getSaveFilename(), dto.getOriginalFilename());
			
		} catch (NullPointerException | StorageException e) {
			log.info("download : ", e);
		} catch (Exception e) {
			log.info("download : ", e);
		}
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("파일 다운로드가 실패 했습니다.");
	}

	// 게시글 좋아요 추가 : AJAX-JSON
	@PostMapping("/like/{num}")
	public ResponseEntity<?> insertBoardLike(
			@PathVariable(name = "num") long num,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		
		try {
			LoginUser loginUser = userDetails.getLoginUser();
			
			int boardLikeCount = 0;
			
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("num", num);
			paramMap.put("member_id", loginUser.getMember_id());
			
			boardService.insertBoardLike(paramMap);
			boardLikeCount = boardService.boardLikeCount(num);
			
			return ResponseEntity.ok(Map.of(
				"boardLikeCount", boardLikeCount
			));			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

	@DeleteMapping("/like/{num}")
	public ResponseEntity<?> deleteBoardLike(@PathVariable(name = "num") long num,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			LoginUser loginUser = userDetails.getLoginUser();
			
			int boardLikeCount = 0;
			
			Map<String, Object> paramMap = new HashMap<>();
			paramMap.put("num", num);
			paramMap.put("member_id", loginUser.getMember_id());
			
			boardService.deleteBoardLike(paramMap);
			
			boardLikeCount = boardService.boardLikeCount(num);
			
			return ResponseEntity.ok(Map.of(
				"boardLikeCount", boardLikeCount
			));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}

}
