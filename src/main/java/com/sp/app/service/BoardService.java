package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.BoardDto;

public interface BoardService {
	public void insertBoard(BoardDto dto, String uploadPath) throws Exception;
	public List<BoardDto> listBoard(Map<String, Object> map);
	public int dataCount(Map<String, Object> map);
	public BoardDto findById(long num);
	public void updateHitCount(long num) throws Exception;
	public BoardDto findByPrev(Map<String, Object> map);
	public BoardDto findByNext(Map<String, Object> map);
	public void updateBoard(BoardDto dto, String uploadPath) throws Exception;
	public void deleteBoard(long num, String uploadPath, Long member_id, int userLevel) throws Exception;
	
	public void insertBoardLike(Map<String, Object> map) throws Exception;
	public void deleteBoardLike(Map<String, Object> map) throws Exception;
	public int boardLikeCount(long num);
	public boolean isUserBoardLiked(Map<String, Object> map);
	
	public boolean deleteUploadFile(String uploadPath, String filename);
}

