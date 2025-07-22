package com.board.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.board.dao.BoardDao;
import com.board.model.Board;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PostDetailController implements Initializable {
    
    @FXML private Label titleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label contentLabel;
    @FXML private HBox actionButtonsBox;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private VBox attachmentBox;
    @FXML private VBox attachmentList;
    @FXML private VBox commentsContainer;
    @FXML private TextArea commentTextArea;
    @FXML private Button submitCommentButton;
    @FXML private Button backButton;
    
    // 데이터 모델
    private static Board boardData;
    private List<Comment> comments;
    private String currentUser = "교수님"; // 현재 로그인한 사용자 (실제로는 세션에서 가져와야 함)
    private static Long boardId;
    
    private final BoardDao boardDao;

    // 의존성 주입 
    public PostDetailController() {
		this.boardDao = new BoardDao().getInstance();
        // 반드시 public, 파라미터 없음
    }
    
    private BoardController boardController;
    // BoardController 참조 설정
    public void setBoardController(BoardController boardController) {
        this.boardController = boardController;
    }
    
    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }
    
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        comments = new ArrayList<>();
        
        // 데이터 로드 
        try {
			loadData(boardId);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        displayPost();
        displayComments();
        
        // 현재 사용자가 작성자인지 확인하여 수정/삭제 버튼 표시
        checkAuthorPermissions();
    }
    
    private void loadData(Long boardId) throws ParseException {
    	
    	// TODO :: DB에서 데이터 호출
        System.out.println("게시물 ID : " + boardId);
        Board board = boardDao.getBoard(boardId);
        
        // 샘플 게시글 데이터
        boardData = board;
        
        // 샘플 댓글 데이터
        comments.add(new Comment(1, 0, "홍길동", "25.06.24", "다시 올려드립니다", currentUser.equals("홍길동")));
        comments.add(new Comment(2, 1, "김철수", "25.06.24", "감사합니다!", currentUser.equals("김철수")));
        comments.add(new Comment(3, 0, "이영희", "25.06.25", "좋은 자료네요", currentUser.equals("이영희")));
    }
    
    private void displayPost() {
        titleLabel.setText(boardData.getTitle());
        authorLabel.setText(boardData.getAuthor());
        dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(boardData.getCreatedAt()));
        contentLabel.setText(boardData.getContent());
        
        // 첨부파일 표시	
        if (!(boardData.getFileId() == null)) {
            attachmentBox.setVisible(true);
            attachmentList.getChildren().clear();
            
          Hyperlink fileLink = new Hyperlink("file test");
          fileLink.setOnAction(e -> downloadFile("file test"));
          fileLink.setStyle("-fx-text-fill: #007bff;");
          attachmentList.getChildren().add(fileLink);
            
            // TODO :: 첨부파일 테이블 연동시  첨부파일 이름 출력 
//            for (Long attachment : boardData.getFileId()) {
//                Hyperlink fileLink = new Hyperlink(attachment);
//                fileLink.setOnAction(e -> downloadFile(attachment));
//                fileLink.setStyle("-fx-text-fill: #007bff;");
//                attachmentList.getChildren().add(fileLink);
//            }
        }
    }
    
    private void displayComments() {
        commentsContainer.getChildren().clear();
        
        for (Comment comment : comments) {
            if (comment.getParentId() == 0) { // 최상위 댓글
                VBox commentBox = createCommentBox(comment, false);
                commentsContainer.getChildren().add(commentBox);
                
                // 대댓글 찾기
                for (Comment reply : comments) {
                    if (reply.getParentId() == comment.getId()) {
                        VBox replyBox = createCommentBox(reply, true);
                        commentsContainer.getChildren().add(replyBox);
                    }
                }
            }
        }
    }
    
    private VBox createCommentBox(Comment comment, boolean isReply) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0; -fx-padding: 10;");
        
        if (isReply) {
            commentBox.setPadding(new Insets(10, 10, 10, 40)); // 대댓글은 들여쓰기
            commentBox.setStyle(commentBox.getStyle() + "-fx-background-color: #f8f9fa;");
        }
        
        // 작성자 정보
        HBox authorInfo = new HBox(10);
        Label authorLabel = new Label(comment.getAuthor());
        authorLabel.setFont(Font.font("DungGeunMo", 14));
        Label dateLabel = new Label(comment.getDate());
        dateLabel.setStyle("-fx-text-fill: #666;");
        
        authorInfo.getChildren().addAll(authorLabel, dateLabel);
        
        // 댓글 내용
        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        
        // 작업 버튼 (작성자만 표시)
        HBox actionButtons = new HBox(10);
        if (comment.isOwner()) {
            Button editBtn = new Button("수정");
            editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-size: 12;");
            editBtn.setOnAction(e -> handleEditComment(comment));
            
            Button deleteBtn = new Button("삭제");
            deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12;");
            deleteBtn.setOnAction(e -> handleDeleteComment(comment));
            
            actionButtons.getChildren().addAll(editBtn, deleteBtn);
        }
        
        // 답글 버튼 (최상위 댓글에만)
        if (!isReply) {
            Button replyBtn = new Button("답글");
            replyBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-size: 12;");
            replyBtn.setOnAction(e -> handleReplyComment(comment));
            actionButtons.getChildren().add(replyBtn);
        }
        
        commentBox.getChildren().addAll(authorInfo, contentLabel, actionButtons);
        
        return commentBox;
    }
    
    // 본인이 작성한 게시물에서만 수정/삭제 버튼 출력 
    private void checkAuthorPermissions() {
        if (boardData.getAuthor().equals(currentUser)) {
            actionButtonsBox.setVisible(true);
        }
    }
    
    // 게시글 수정 
    @FXML
    private void handleEditPost() {
        // 게시글 수정 페이지로 이동 (미구현)
    	// TODO :: 유저 연동 시 권한 체크  
    	String userRole = "MANAGER";
//    	String userRole = "STUDENT";
        try {
        	// 권한이 매니저나 교수일 경우
        	if(userRole.equals("MANAGER") || userRole.equals( "PROFESSOR")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/board/PostWrite.fxml"));
                Stage stage = new Stage();
                stage.setTitle("공지사항 및 과제 작성");
                stage.setScene(new Scene(loader.load()));
                stage.initModality(Modality.APPLICATION_MODAL);

                
                PostWriteController controller = loader.getController();
                controller.setPostDetailController(this);

                stage.showAndWait();
        	}else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/board/BoardWrite.fxml"));
                Stage stage = new Stage();
                stage.setTitle("게시글 작성");
                stage.setScene(new Scene(loader.load()));
                stage.initModality(Modality.APPLICATION_MODAL);

                BoardWriteController controller = loader.getController();
                controller.setPostDetailController(this);

                stage.showAndWait();
        	}
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    	
    	
        showAlert("수정", "게시글 수정 기능은 아직 구현되지 않았습니다.");
    }
    
    
    // 게시글 삭제  
    @FXML
    private void handleDeletePost(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("게시글 삭제");
        alert.setHeaderText("정말로 이 게시글을 삭제하시겠습니까?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 게시글 삭제 로직 (미구현)
            showAlert("삭제", "게시글이 삭제되었습니다.");
            handleBack(event);
        }
    }
    
    @FXML
    private void handleSubmitComment() {
        String commentText = commentTextArea.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("경고", "댓글 내용을 입력해주세요.");
            return;
        }
        
        // 새 댓글 추가
        int newId = comments.stream().mapToInt(Comment::getId).max().orElse(0) + 1;
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd"));
        Comment newComment = new Comment(newId, 0, currentUser, currentDate, commentText, true);
        
        comments.add(newComment);
        commentTextArea.clear();
        displayComments();
    }
    
    private void handleEditComment(Comment comment) {
        // 댓글 수정 (미구현)
        showAlert("수정", "댓글 수정 기능은 아직 구현되지 않았습니다.");
    }
    
    private void handleDeleteComment(Comment comment) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("댓글 삭제");
        alert.setHeaderText("정말로 이 댓글을 삭제하시겠습니까?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 댓글과 대댓글 모두 삭제
            comments.removeIf(c -> c.getId() == comment.getId() || c.getParentId() == comment.getId());
            displayComments();
        }
    }
    
    private void handleReplyComment(Comment parentComment) {
        String replyText = commentTextArea.getText().trim();
        if (replyText.isEmpty()) {
            showAlert("경고", "답글 내용을 입력해주세요.");
            return;
        }
        
        // 대댓글 추가
        int newId = comments.stream().mapToInt(Comment::getId).max().orElse(0) + 1;
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd"));
        Comment reply = new Comment(newId, parentComment.getId(), currentUser, currentDate, replyText, true);
        
        comments.add(reply);
        commentTextArea.clear();
        displayComments();
    }
    
    private void downloadFile(String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("파일 저장");
        fileChooser.setInitialFileName(fileName);
        
        File saveFile = fileChooser.showSaveDialog(null);
        if (saveFile != null) {
            try {
                // 실제로는 서버에서 파일을 다운로드해야 함
                // 여기서는 샘플 파일 생성
                Files.write(saveFile.toPath(), "샘플 파일 내용입니다.".getBytes());
                showAlert("다운로드", "파일이 성공적으로 다운로드되었습니다.");
            } catch (IOException e) {
                showAlert("오류", "파일 다운로드 중 오류가 발생했습니다.");
            }
        }
    }
    
    @FXML
    private void handleBack(ActionEvent event) {
        // 현재 버튼이 속한 Scene의 Stage를 얻어서 닫기
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


class Comment {
    private int id;
    private int parentId;
    private String author;
    private String date;
    private String content;
    private boolean isOwner;
    
    public Comment(int id, int parentId, String author, String date, String content, boolean isOwner) {
        this.id = id;
        this.parentId = parentId;
        this.author = author;
        this.date = date;
        this.content = content;
        this.isOwner = isOwner;
    }
    
    // Getters
    public int getId() { return id; }
    public int getParentId() { return parentId; }
    public String getAuthor() { return author; }
    public String getDate() { return date; }
    public String getContent() { return content; }
    public boolean isOwner() { return isOwner; }
}