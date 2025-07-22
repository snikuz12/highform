package com.board.controller;

import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;

import com.board.dao.BoardDao;
import com.board.model.Board;
import com.board.model.BoardCategory;
import com.board.model.dto.BoardDto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BoardController {
    @FXML private Label pathLabel;
    @FXML private Button noticeBtn, resourceBtn, boardBtn, backBtn, uploadBtn;
    @FXML private TableView<BoardDto> boardTable;
    @FXML private TableColumn<BoardDto, Integer> noColumn;
    @FXML private TableColumn<BoardDto, String> titleColumn;
    @FXML private TableColumn<BoardDto, String> authorColumn;
    @FXML private TableColumn<BoardDto, String> dateColumn;
    @FXML private Pagination pagination;

    private ObservableList<BoardDto> allItems = FXCollections.observableArrayList();
    private ObservableList<BoardDto> currentItems = FXCollections.observableArrayList();
    private BoardCategory currentBoardType = BoardCategory.NOTICE;
    private static final int ROWS_PER_PAGE = 16;
    private static final double ROW_HEIGHT = 32.0;
    private static final double HEADER_HEIGHT = 32.0;

    private final BoardDao boardDao;

    public BoardController() {
		this.boardDao = new BoardDao().getInstance();
        // 반드시 public, 파라미터 없음
    }
    
    @FXML
    public void initialize() throws ParseException {
        // 테이블 높이 고정 설정
        boardTable.setFixedCellSize(ROW_HEIGHT);
        double tableHeight = ROWS_PER_PAGE * ROW_HEIGHT + HEADER_HEIGHT;
        boardTable.setPrefHeight(tableHeight);
        boardTable.setMinHeight(tableHeight);
        boardTable.setMaxHeight(tableHeight);

        
        // DB 게시물 데이터 호출 후 저장
        allItems.addAll (boardDao.getBoardList(BoardCategory.BOARD));
        allItems.addAll (boardDao.getBoardList(BoardCategory.DATA_ROOM));
        allItems.addAll (boardDao.getBoardList(BoardCategory.NOTICE));
        

        // 컬럼 바인딩
        noColumn.setCellValueFactory(cellData -> cellData.getValue().noProperty().asObject());
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());

        
        boardTable.setRowFactory(tv -> {
            TableRow<BoardDto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    BoardDto clicked = row.getItem();
					try {
	                    // 상세 페이지 이동 로직 (추후 구현)
	                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/board/BoardDetail.fxml"));
	                  
	                    Parent root;
	                    
	                    // PostDetailController 인스턴스 얻기
	                    PostDetailController detailController = new PostDetailController();
	                    // boardId 전달
		                detailController.setBoardId(clicked.getBoardId());
	                    
						root = loader.load();
	                    Scene scene = new Scene(root, 1000, 750);
	                    Stage stage = new Stage();
	                    stage.setTitle("게시글 상세보기");
	                    stage.setScene(scene);
	                    stage.setResizable(true);
	                    stage.show();

	                    
	                    
	                    System.out.println("상세 페이지 이동: " + clicked.getTitle() + ", 게시글 ID :"+clicked.getBoardId());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
            });
            return row;
        });

        
        // 초기화 - PageFactory는 한 번만 설정
        filterByType(BoardCategory.NOTICE);
        pagination.setPageFactory(this::createPage);
        updatePagination();
    }

    // 공지사항 게시물 리스트 
    @FXML
    private void handleNoticeBtn(ActionEvent event) {
        currentBoardType = BoardCategory.NOTICE;
        pathLabel.setText("C:\\Board\\Notice");
        filterByType(BoardCategory.NOTICE);
        updatePagination();
    }

    // 자료실 게시물 리스트 
    @FXML
    private void handleResourceBtn(ActionEvent event) {
        currentBoardType = BoardCategory.DATA_ROOM;
        pathLabel.setText("C:\\Board\\DataRoom");
        filterByType(BoardCategory.DATA_ROOM);
        updatePagination();
    }

    // 게시판 게시물 리스트 
    @FXML
    private void handleBoardBtn(ActionEvent event) {
        currentBoardType = BoardCategory.BOARD;
        pathLabel.setText("C:\\Board\\Board");
        filterByType(BoardCategory.BOARD);
        updatePagination();
    }

    @FXML
    private void handleBackBtn(ActionEvent event) {
        // 추후 이전 페이지 이동 구현
        System.out.println("이전 페이지로 이동");
        // TODO :: Main fxml 페이지 연결
    }

    @FXML
    private void handleUploadBtn(ActionEvent event) {
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
                controller.setBoardController(this);

                stage.showAndWait();
        	}else {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/board/BoardWrite.fxml"));
                Stage stage = new Stage();
                stage.setTitle("게시글 작성");
                stage.setScene(new Scene(loader.load()));
                stage.initModality(Modality.APPLICATION_MODAL);

                BoardWriteController controller = loader.getController();
                controller.setBoardController(this);

                stage.showAndWait();
        	}
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterByType(BoardCategory type) {
        currentItems.clear();
        if (type.equals(BoardCategory.BOARD)) {
            allItems.stream().filter(item -> BoardCategory.BOARD.equals(item.getType()))
            .forEach(currentItems::add);
        } else if (type.equals(BoardCategory.NOTICE)) {
        	System.out.println(allItems.getFirst().getType().toString());
        	System.out.println(BoardCategory.NOTICE == allItems.getFirst().getType());
        	System.out.println(BoardCategory.NOTICE.equals(allItems.getFirst().getType()));
        	
        	
            allItems.stream().filter(item -> BoardCategory.NOTICE == item.getType())
                    .forEach(currentItems::add);
        } else if (type.equals(BoardCategory.DATA_ROOM)) {
            allItems.stream().filter(item -> BoardCategory.DATA_ROOM.equals(item.getType()))
                    .forEach(currentItems::add);
        }
    }

    // 수정된 createPage 메서드 - TableView 대신 더미 Node 반환
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, currentItems.size());
        ObservableList<BoardDto> pageItems = FXCollections.observableArrayList();

        // 실제 데이터 추가
        if (fromIndex < currentItems.size()) {
            pageItems.addAll(currentItems.subList(fromIndex, toIndex));
        }
        
        // 빈 행으로 채우기 (항상 10개 행이 표시되도록)
        for (int i = pageItems.size(); i < ROWS_PER_PAGE; i++) {
            pageItems.add(new BoardDto(0, "", "", null,null,"",null,null));
        }
        
        // TableView에 데이터 설정
        boardTable.setItems(pageItems);
        
        // TableView 높이 재설정 (중요!)
        double tableHeight = ROWS_PER_PAGE * ROW_HEIGHT + HEADER_HEIGHT;
        boardTable.setPrefHeight(tableHeight);
        boardTable.setMinHeight(tableHeight);
        boardTable.setMaxHeight(tableHeight);
        
        // 더미 노드 반환 (Pagination은 이 노드를 표시하지만 실제로는 TableView를 사용)
        return new Region();
    }

    // 수정된 updatePagination 메서드 - PageFactory 재설정 제거
    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) currentItems.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setCurrentPageIndex(0);
        
        // PageFactory 재설정 제거 - initialize()에서 한 번만 설정
        // 첫 페이지 데이터 로드
        createPage(0);
    }

    
    // 게시글 작성 후 리스트에 추가
    // TODO :: User 연동되면 USerId 추가
    public void addNewPost(Board board) {
        int no = allItems.size() + 1;
        Long userId = 2L;
        BoardDto dto =  new BoardDto(no, board.getTitle(), "test", board.getCreatedAt() , board.getType() ,board.getContent(), board.getBoardId(),userId);
        allItems.add(dto);
        filterByType(currentBoardType);
        updatePagination();
    }
    
}