package com.mypage.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.login.model.User;
import com.login.controller.DesktopController; 
import com.mypage.Model.attendance.Attendance;
import com.mypage.dao.attendance.AttendanceDAO;
import java.time.format.DateTimeFormatter; 

public class AttendanceController {
	@FXML
	private Button closeButton;  // 버튼 
    // =====================
    // FXML 컴포넌트 선언
    // =====================
    @FXML private VBox attendanceListBox;      // 출결 리스트를 표시할 VBox (헤더 + 행)
    @FXML private Label attendanceRateLabel;   // 출결률 표시 라벨
    @FXML private Label pageLabel;             // 현재 페이지 표시 라벨
    @FXML private HBox paginationBox;          // 페이지네이션 버튼 표시 HBox

    // =====================
    // 페이징/데이터 변수
    // =====================
    private int page = 1;              // 현재 페이지 번호
    private final int size = 15;        // 한 페이지당 데이터 개수
    private int totalCount = 0;        // 전체 출결 데이터 개수
    private int totalPages = 1;        // 전체 페이지 개수
    private Long userId;               // 사용자 ID (로그인 연동 시 동적으로 할당)

    // =====================
    // DAO 싱글톤 사용
    // =====================
    private AttendanceDAO attendanceDAO = AttendanceDAO.getInstance();

    // =====================
    // 사용자 정보 (중복 필드 정리)
    // =====================
    private User currentUser;  // 하나의 필드만 사용

    // =====================
    // 화면 초기화 시 호출
    // =====================
    @FXML
    private void initialize() {
        // userId는 setCurrentUser에서 설정하도록 변경
        loadAttendanceList();   // 출결 리스트 로드
        loadAttendanceRate();   // 출결률 로드
    }

    /**
     * 현재 사용자 설정 (DesktopController에서 호출)
     */
    public void setCurrentUser(User user) {
        if (user == null) {
            System.err.println("[ERROR] AttendanceController.setCurrentUser: user가 null입니다.");
            return;
        }
        
        this.currentUser = user;  // 하나의 필드만 사용
        this.userId = user.getId();  // User 객체에서 ID 가져오기
        System.out.println("[DEBUG] AttendanceController currentUser 설정: " + user.getName() + " (ID: " + userId + ")");
        
        // 사용자 정보 설정 후 데이터 다시 로드
        loadAttendanceList();
        loadAttendanceRate();
    }
    
    @FXML //창닫기같은 뒤로가기 - 사용자 정보 유지하며 데스크탑으로 복귀
    private void handleCloseButton() {
        try {
            // 현재 스테이지 가져오기
            Stage stage = (Stage) closeButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login/Desktop.fxml"));
            Parent root = loader.load();

            // DesktopController에 사용자 정보 전달
            DesktopController desktopController = loader.getController();
            if (desktopController != null && currentUser != null) {
                desktopController.setCurrentUser(currentUser);
                System.out.println("[DEBUG] 데스크탑으로 사용자 정보 전달: " + currentUser.getName());
            } else {
                System.err.println("[ERROR] DesktopController 또는 currentUser가 null입니다.");
            }

            // 새로운 씬 설정
            Scene scene = new Scene(root);
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            showError("페이지 이동 오류", "데스크탑으로 이동할 수 없습니다.");
        }
    }
    
    /* -----------------------------------------------------------
     * 출결 리스트 로드
     * ----------------------------------------------------------- */
    private void loadAttendanceList() {
        // userId가 null인 경우 임시값 사용 (initialize에서 호출될 때)
        if (userId == null) {
            userId = 1L; // 임시값
        }
        
        try {
            int offset = (page - 1) * size;

            List<Attendance> list   = attendanceDAO.getAttendanceList(userId, offset, size);
            totalCount              = attendanceDAO.getAttendanceCount(userId);

            /* 헤더(HBox 0번) 제외, 기존 행 제거 */
            if (attendanceListBox.getChildren().size() > 1) {
                attendanceListBox.getChildren()
                                 .remove(1, attendanceListBox.getChildren().size());
            }

            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

            /* ▼ 데이터 행 생성 */
            for (int i = 0; i < list.size(); i++) {
                Attendance att = list.get(i);
                HBox row = new HBox();
                row.setStyle("-fx-background-color:#f0f0f0;"
                           + "-fx-border-color:black;"
                           + "-fx-border-width:0 0 1 0;");

                /* ① 일련번호 */
                row.getChildren().add(makeCell(String.valueOf(offset + i + 1),  90));

                /* ② 출결 상태(enum → 한글 설명) */
                String statusStr = att.getStatus() != null
                                 ? att.getStatus().getDescription()
                                 : "";
                row.getChildren().add(makeCell(statusStr, 160));

                /* ③ 날짜 (attendanceDate) */
                String dateStr = att.getAttendanceDate() != null
                               ? att.getAttendanceDate().toString()
                               : "";
                row.getChildren().add(makeCell(dateStr, 210));

                /* ④ 입실 시각 */
                String inStr = att.getCheckIn() != null
                             ? att.getCheckIn().toLocalTime().format(timeFmt)
                             : "";
                row.getChildren().add(makeCell(inStr, 255));

                /* ⑤ 퇴실 시각 */
                String outStr = att.getCheckOut() != null
                              ? att.getCheckOut().toLocalTime().format(timeFmt)
                              : "";
                row.getChildren().add(makeCell(outStr, 255));

                attendanceListBox.getChildren().add(row);
            }

            /* 페이지네이션 갱신 */
            updatePagination();

            if (pageLabel != null) pageLabel.setText("페이지: " + page);

        } catch (SQLException e) {
            showError("출결 내역 불러오기 실패", e.getMessage());
        }
    }

    /**
     * 동적으로 페이지네이션 버튼(Prev, 1~N, Next) 생성 및 이벤트 등록
     *  - 10개씩 페이징 그룹 단위로 보임(11페이지 이상부터 우측으로 이동)
     */
    private void updatePagination() {
        paginationBox.getChildren().clear(); // 기존 버튼/라벨 모두 삭제
        totalPages = (int) Math.ceil(totalCount / (double) size); // 전체 페이지 계산

        int maxBtn = 15; // 한 번에 최대 노출 페이지 버튼 수
        int startPage = ((page - 1) / maxBtn) * maxBtn + 1;
        int endPage = Math.min(startPage + maxBtn - 1, totalPages);

        // Prev 버튼 (맨 앞 페이지 아니면 활성화)
        Button prev = new Button("<");
        prev.setOnAction(e -> {
            if (page > 1) {
                page--;
                loadAttendanceList();
            }
        });
        paginationBox.getChildren().add(prev);

        // 페이지 번호 버튼 동적 생성
        for (int i = startPage; i <= endPage; i++) {
            Button btn = new Button(String.valueOf(i));
            if (i == page) btn.setStyle("-fx-background-color: #00007b; -fx-text-fill: white;"); // 현재 페이지 강조
            btn.setOnAction(e -> {
                page = Integer.parseInt(btn.getText());
                loadAttendanceList();
            });
            paginationBox.getChildren().add(btn);
        }

        // Next 버튼 (마지막 페이지 아니면 활성화)
        Button next = new Button(">");
        next.setOnAction(e -> {
            if (page < totalPages) {
                page++;
                loadAttendanceList();
            }
        });
        paginationBox.getChildren().add(next);
    }

    /**
     * 출결 한 행의 셀(Label) 생성 헬퍼
     */
    private Label makeCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setPrefHeight(32);
        label.setStyle("-fx-font-size: 17px; -fx-alignment: center; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        label.setAlignment(javafx.geometry.Pos.CENTER);
        return label;
    }

    /**
     * 출결률(%) DB에서 조회해서 라벨에 세팅
     */
    private void loadAttendanceRate() {
        // userId가 null인 경우 임시값 사용
        if (userId == null) {
            userId = 1L; // 임시값
        }
        
        try {
            double rate = attendanceDAO.getAttendanceRate(userId);
            attendanceRateLabel.setText("출결률: " + rate + "%");
        } catch (SQLException e) {
            attendanceRateLabel.setText("출결률: 오류");
        }
    }

    /**
     * 에러 알림창
     */
    private void showError(String header, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    @FXML
    private void handleApplyBtn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mypage/attendance/approval_request_form.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("휴가/공결 신청");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // 현재 창 잠금(선택)
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "신청 창 열기 실패: " + e.getMessage()).showAndWait();
        }
    }
}