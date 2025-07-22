package com.board.model;

import com.util.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Board extends BaseEntity{
	
	private Long boardId;
	private String author;
	private String title;
	private String content;
	private BoardCategory type;
	private Long fileId;
	
	// TODO :: 유저 연동되면 매핑
	private Long userId;
}
