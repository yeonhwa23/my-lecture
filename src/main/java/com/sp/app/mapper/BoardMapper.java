package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.BoardDto;

@Mapper
public interface BoardMapper {
	public void insertBoard(BoardDto dto) throws SQLException;
	public void updateBoard(BoardDto dto) throws SQLException;
	public void deleteBoard(long num) throws SQLException;
	
	public int dataCount(Map<String, Object> map);
	public List<BoardDto> listBoard(Map<String, Object> map);
	
	public BoardDto findById(Long num);
	public void updateHitCount(long num) throws SQLException;
	public BoardDto findByPrev(Map<String, Object> map);
	public BoardDto findByNext(Map<String, Object> map);
	
	public void insertBoardLike(Map<String, Object> map) throws SQLException;
	public void deleteBoardLike(Map<String, Object> map) throws SQLException;
	public int boardLikeCount(long num);
	public BoardDto hasUserBoardLiked(Map<String, Object> map);
}

