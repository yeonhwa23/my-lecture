package com.sp.app.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardDto {
	private long num;
	private Long member_id;
	private String name;
	private String subject;
	private String content;
	private String reg_date;
	private int hitCount;
	private int block;
	
	private String saveFilename;
	private String originalFilename;
	private MultipartFile selectFile; // <input type='file' name='selectFile' ..
	
	private int replyCount;
	private int boardLikeCount;
	
	private boolean canEdit;
	private boolean canDelete;
}
