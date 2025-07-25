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

    @FXML private Button closeButton;

    @FXML private VBox attendanceListBox;      
    @FXML private Label attendanceRateLabel;   
    @FXML private Label pageLabel;             
    @FXML private HBox paginationBox;          

    // 페이징 관련 필드
    private int page = 1;
    private final int size = 15;
    private int totalCount = 0;
    private int totalPages = 1;

    // 현재 로그인 사용자 객체 및 id
    private User currentUser;
    private Long userId;      // 항상 currentUser에서 얻음

    private AttendanceDAO attendanceDAO = AttendanceDAO.getInstance();

    // =============== 화면 초기화 =================
    @FXML
    private void initialize() {
        // setCurrentUser 호출되기 전에는 더미값(1L) 사용 (null-safe)
        if (userId == null) userId = 1L;
        loadAttendanceList();
        loadAttendanceRate();
    }

    /**
     * 로그인 후 반드시 호출: 현재 세션 사용자 정보 보관 및 데이터 로드
     */
    public void setCurrentUser(User user) {
        if (user == null) {
            System.err.println("[ERROR] AttendanceController.setCurrentUser: user가 null입니다.");
            return;
        }
        this.currentUser = user;
        this.userId = user.getId();
        System.out.println("[DEBUG] AttendanceController currentUser 설정: " + user.getName() + " (ID: " + userId + ")");

        // 세션 사용자를 기준으로 출결 데이터 새로 로드
        loadAttendanceList();
        loadAttendanceRate();
    }

    /**
     * 데스크탑으로 이동(뒤로가기/닫기) - 사용자 정보 유지
     */
    @FXML
    private void handleCloseButton() {
        try {
            Stage stage = (Stage) closeButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login/Desktop.fxml"));
            Parent root = loader.load();

            // DesktopController에 User 정보 전달
            DesktopController desktopController = loader.getController();
            if (desktopController != null && currentUser != null) {
                desktopController.setCurrentUser(currentUser);
                System.out.println("[DEBUG] 데스크탑으로 사용자 정보 전달: " + currentUser.getName());
            } else {
                System.err.println("[ERROR] DesktopController 또는 currentUser가 null입니다.");
            }
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showError("페이지 이동 오류", "데스크탑으로 이동할 수 없습니다.");
        }
    }

    // ------------------ 출결 리스트 로드 ------------------
    private void loadAttendanceList() {
        // userId가 null인 경우 임시값 사용
        if (userId == null) userId = 1L;
        try {
            int offset = (page - 1) * size;
            List<Attendance> list = attendanceDAO.getAttendanceList(userId, offset, size);
            totalCount = attendanceDAO.getAttendanceCount(userId);

            // 헤더 제외 기존 행 제거
            if (attendanceListBox.getChildren().size() > 1) {
                attendanceListBox.getChildren().remove(1, attendanceListBox.getChildren().size());
            }

            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            for (int i = 0; i < list.size(); i++) {
                Attendance att = list.get(i);
                HBox row = new HBox();
                row.setStyle("-fx-background-color:#f0f0f0;"
                        + "-fx-border-color:black;"
                        + "-fx-border-width:0 0 1 0;");

                // ① 일련번호
                row.getChildren().add(makeCell(String.valueOf(offset + i + 1),  90));
                // ② 출결 상태(한글)
                String statusStr = att.getStatus() != null ? att.getStatus().getDescription() : "";
                row.getChildren().add(makeCell(statusStr, 160));
                // ③ 날짜
                String dateStr = att.getAttendanceDate() != null ? att.getAttendanceDate().toString() : "";
                row.getChildren().add(makeCell(dateStr, 210));
                // ④ 입실
                String inStr = att.getCheckIn() != null ? att.getCheckIn().toLocalTime().format(timeFmt) : "";
                row.getChildren().add(makeCell(inStr, 255));
                // ⑤ 퇴실
                String outStr = att.getCheckOut() != null ? att.getCheckOut().toLocalTime().format(timeFmt) : "";
                row.getChildren().add(makeCell(outStr, 255));

                attendanceListBox.getChildren().add(row);
            }
            updatePagination();
            if (pageLabel != null) pageLabel.setText("페이지: " + page);
        } catch (SQLException e) {
            showError("출결 내역 불러오기 실패", e.getMessage());
        }
    }

    // ------------------ 출결률 로드 ------------------
    private void loadAttendanceRate() {
        if (userId == null) userId = 1L;
        try {
            double rate = attendanceDAO.getAttendanceRate(userId);
            attendanceRateLabel.setText("출결률: " + rate + "%");
        } catch (SQLException e) {
            attendanceRateLabel.setText("출결률: 오류");
        }
    }

    // ------------------ 페이지네이션 갱신 ------------------
    private void updatePagination() {
        paginationBox.getChildren().clear();
        totalPages = (int) Math.ceil(totalCount / (double) size);

        int maxBtn = 15;
        int startPage = ((page - 1) / maxBtn) * maxBtn + 1;
        int endPage = Math.min(startPage + maxBtn - 1, totalPages);

        // Prev
        Button prev = new Button("<");
        prev.setOnAction(e -> {
            if (page > 1) {
                page--;
                loadAttendanceList();
            }
        });
        paginationBox.getChildren().add(prev);

        // 번호 버튼
        for (int i = startPage; i <= endPage; i++) {
            Button btn = new Button(String.valueOf(i));
            if (i == page) btn.setStyle("-fx-background-color: #00007b; -fx-text-fill: white;");
            btn.setOnAction(e -> {
                page = Integer.parseInt(btn.getText());
                loadAttendanceList();
            });
            paginationBox.getChildren().add(btn);
        }

        // Next
        Button next = new Button(">");
        next.setOnAction(e -> {
            if (page < totalPages) {
                page++;
                loadAttendanceList();
            }
        });
        paginationBox.getChildren().add(next);
    }

    // ------------------ 셀(Label) 생성 헬퍼 ------------------
    private Label makeCell(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setPrefHeight(32);
        label.setStyle("-fx-font-size: 17px; -fx-alignment: center; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        label.setAlignment(javafx.geometry.Pos.CENTER);
        return label;
    }

    // ------------------ 에러 알림 ------------------
    private void showError(String header, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ------------------ 휴가/공결 신청 팝업 ------------------
    @FXML
    private void handleApplyBtn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mypage/attendance/approval_request_form.fxml"));
            Parent root = loader.load();

            // 팝업 컨트롤러에 currentUser 정보 넘기고 싶으면 여기에 세팅 (추가 구현 필요)
            // ApprovalRequestController approvalCtrl = loader.getController();
            // if (approvalCtrl != null && currentUser != null) {
            //     approvalCtrl.setCurrentUser(currentUser);
            // }

            Stage stage = new Stage();
            stage.setTitle("휴가/공결 신청");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "신청 창 열기 실패: " + e.getMessage()).showAndWait();
        }
    }
}
