package com.board.controller;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.board.dao.BoardDao;
import com.board.model.Board;
import com.board.model.BoardCategory;
import com.board.model.dto.BoardDto;
import com.board.model.dto.BoardWriteRequestDto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PostWriteController {
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private TextField attachmentField;
    @FXML private Button browseBtn, submitBtn;

    private BoardController boardController;
    private PostDetailController postDetailController;
    private BoardCategory selectedType = BoardCategory.DATA_ROOM;
    private String attachmentPath = "";
    private final BoardDao boardDao;
    
    
    public PostWriteController() {
		this.boardDao = new BoardDao().getInstance();
        // 반드시 public, 파라미터 없음
    }
    
    public void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
    
    public void setPostDetailController(PostDetailController postDetailController) {
    	this.postDetailController = postDetailController;
    }
    
    
    
    @FXML
    public void initialize() {
        typeComboBox.getItems().addAll("과제", "공지사항");
        typeComboBox.setValue("과제");
        typeComboBox.setOnAction(e -> selectedType = typeComboBox.getValue().equals("과제") ? BoardCategory.DATA_ROOM : BoardCategory.NOTICE);
    }



    @FXML
    private void handleBrowseBtn(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("첨부파일 선택");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("All Files", "*.*");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage) browseBtn.getScene().getWindow();
        java.io.File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            attachmentPath = file.getAbsolutePath();
            attachmentField.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmitBtn(ActionEvent event) throws ParseException {
        String title = titleField.getText().trim();
        String content = contentArea.getText().trim();
        String author = "교수님"; // 실제로는 로그인한 사용자 정보를 가져와야 함
        if (title.isEmpty() || content.isEmpty()) {
            showAlert("제목과 내용을 입력하세요.");
            return;
        }
        // TODO :: DB 저장하고 ID값 반환
        // TODO :: User 연동하고 ID값 반환 
      
//        newItem.setAttachmentPath(attachmentPath);
    
        Long fileId = 1L;
        Long userId = 1L;
        BoardWriteRequestDto newPost = new BoardWriteRequestDto(1, title, author,  selectedType,content,fileId );
        
       
        Board board = newPost.toEntity(newPost, fileId, userId);
        
        // DB 데이터 저장 
        Long boardId = boardDao.createBoard(board);
        
        
        // DB 데이터 호출
        Board boardEntity = boardDao.getBoard(boardId);
        
        

        if (boardController != null) {
            boardController.addNewPost(boardEntity);
        }
        ((Stage) submitBtn.getScene().getWindow()).close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("입력 오류");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}