package com.board.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.board.dao.BoardDao;
import com.board.model.Board;
import com.board.model.BoardCategory;
import com.board.model.dto.BoardWriteRequestDto;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
@AllArgsConstructor
public class BoardWriteController {
    
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private TextField filePathField;
    @FXML private CheckBox aiQuestionCheck;
    @FXML private Button browseBtn, submitBtn, closeBtn;
    
    private BoardController boardController;
    private PostDetailController postDetailController;
    private File selectedFile;
    
    private final BoardDao boardDao;

    public BoardWriteController() {
		this.boardDao = new BoardDao().getInstance();
        // 반드시 public, 파라미터 없음
    }
    
    // BoardController 참조 설정
    public void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
    
    public void setPostDetailController(PostDetailController postDetailController) {
    	this.postDetailController = postDetailController;
    }
    
    
    @FXML
    public void initialize() {
        // 초기화 코드 (필요시)
    }
    

    
    @FXML
    private void handleBrowseBtn(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("첨부파일 선택");
        
        // 파일 확장자 필터 설정
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("모든 파일", "*.*"),
            new FileChooser.ExtensionFilter("텍스트 파일", "*.txt"),
            new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.gif"),
            new FileChooser.ExtensionFilter("Java 파일", "*.java"),
            new FileChooser.ExtensionFilter("문서 파일", "*.pdf", "*.doc", "*.docx")
        );
        
        Stage stage = (Stage) browseBtn.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            filePathField.setText(selectedFile.getName());
        }
    }
    
    @FXML
    private void handleSubmitBtn(ActionEvent event) {
        // 입력 검증
        if (titleField.getText().trim().isEmpty()) {
            showAlert("경고", "제목을 입력해주세요.");
            return;
        }
        
        if (contentArea.getText().trim().isEmpty()) {
            showAlert("경고", "내용을 입력해주세요.");
            return;
        }
        
        try {
            // 새 게시글 생성
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();
            String author = "현재사용자"; // 실제로는 로그인한 사용자 정보를 가져와야 함
            BoardCategory type = BoardCategory.BOARD;
            
            // 첨부파일 정보가 있다면 처리
            if (selectedFile != null) {
                // 파일 처리 로직 (파일 복사, 경로 저장 등)
                System.out.println("첨부파일: " + selectedFile.getAbsolutePath());
            }
            
            
            // BoardDto 객체 생성 (생성자에 맞게 수정 필요)
            // TODO :: File Table 생성 후 ID 가져와야함, User Id값 가져와야함 

            Long fileId = 1L;
            Long userId = 1L;
            BoardWriteRequestDto newPost = new BoardWriteRequestDto(1, title, author,  type,content,fileId );
            
 
            Board board = newPost.toEntity(newPost, fileId, userId);
            
            // DB 데이터 저장 Entity 값을 반환받을 수가 없기에 boardId값을 반환받아 사용 
            Long boardId = boardDao.createBoard(board);
            
            
            // DB 데이터 호출 - DB에 저장하는 시점에 시간 저장이 되기 때문에 다시 게시물에 대한 값을 받아와야함 
            Board boardEntity = boardDao.getBoard(boardId);
            
            
            // AI 질문 여부 처리
            if (aiQuestionCheck.isSelected()) {
                // AI 질문 관련 처리
                System.out.println("AI 질문 게시글로 설정됨");
            }
            
            
            // DB에서 방금 저장된 Entity 호출
            
            
            // BoardController에 새 게시글 추가
            if (boardController != null) {
                boardController.addNewPost(boardEntity);
            }
            
            showAlert("성공", "게시글이 성공적으로 작성되었습니다.");
            closeWindow();
            
        } catch (Exception e) {
            showAlert("오류", "게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCloseBtn(ActionEvent event) {
        closeWindow();
    }

   
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) submitBtn.getScene().getWindow();
        stage.close();
    }
    
    // 폼 초기화 메서드
    public void clearForm() {
        titleField.clear();
        contentArea.clear();
        filePathField.clear();
        aiQuestionCheck.setSelected(false);
        selectedFile = null;
    }
}