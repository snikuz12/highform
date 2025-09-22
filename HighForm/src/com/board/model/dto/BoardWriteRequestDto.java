package com.board.model.dto;

import com.board.model.Board;
import com.board.model.BoardCategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class BoardWriteRequestDto {
	private int no;
    private String title;
    private String author;
    private BoardCategory type;
    private String content;
    private Long filePath;

    
    public BoardWriteRequestDto(int no, String title, String author, BoardCategory type) {
        this();
        setNo(no);
        setTitle(title);
        setAuthor(author);
        setType(type);
    }
    
    public BoardWriteRequestDto(int no, String title, String author, BoardCategory type, 
                   String content, Long filePath) {
        this(no, title, author, type);
        setContent(content);
        setFilePath(filePath);
    }
    
    
    @Override
    public String toString() {
        return "BoardDto{" +
                "no=" + getNo() +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", type='" + getType() + '\'' +
                '}';
    }
	
    
    public static Board toEntity(BoardWriteRequestDto dto, Long fileId, Long userId) {
    	return Board.builder()
    			.author(dto.author)
    			.title(dto.title)
    			.content(dto.content)
    			.type(dto.getType())
    			.fileId(fileId)					// TODO :: Filed Table 생성시 연결
    			.userId(userId)			// TODO :: 유저 연동되면 매핑
    			.build();
    	
    }
}
