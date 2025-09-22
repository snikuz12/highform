package com.board.model.dto;

import java.sql.Date;
import java.text.SimpleDateFormat;

import com.board.model.BoardCategory;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BoardDto {

	private IntegerProperty no;
	private StringProperty title;
	private StringProperty author;
	private StringProperty date;
	private BoardCategory type;
	private StringProperty content;
	private StringProperty attachmentPath;
	private Long boardId;
	private Long userId;
    
    public BoardDto(int no, String title, String author, Date date, BoardCategory type,  String content, Long boardId, Long userId) {
        this.no = new SimpleIntegerProperty(no);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.date = new SimpleStringProperty(date != null ? new SimpleDateFormat("yyyy-MM-dd").format(date) : null);
        this.type = type;
        this.content = new SimpleStringProperty(content);
        this.attachmentPath = new SimpleStringProperty("");
        this.boardId = boardId;
        this.userId = userId;
    }

    // Getters and Setters
    public void setNo(int no) { this.no.set(no); }
    public IntegerProperty noProperty() { return no; }
    
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }
    
    public void setAuthor(String author) { this.author.set(author); }
    public StringProperty authorProperty() { return author; }
    
    public void setDate(String date) { this.date.set(date); }
    public StringProperty dateProperty() { return date; }
    
    public void setType(BoardCategory type) { this.type = type; }
    public BoardCategory typeProperty() { return type; }
    
    public void setContent(String content) { this.content.set(content); }
    public StringProperty contentProperty() { return content; }
    
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath.set(attachmentPath); }
    public StringProperty attachmentPathProperty() { return attachmentPath; }
    
    public void setBoardId(Long id) {this.boardId = id;}
    
   

}