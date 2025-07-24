package com.mypage.controller;

import com.login.controller.DesktopController;
import com.login.model.User;
import com.mypage.Model.attendance.AttendanceApprovalRequest;
import com.mypage.dao.attendance.AttendanceApprovalRequestDAO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;

public class ApprovalRequestController {
    @FXML private TextArea reasonArea;
    @FXML private TextField fileField;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private Button fileBtn;
    @FXML private Button closeButton;

    private User  currentUser;
    private Long  userId;

    @FXML
    public void initialize() {
        // 파일 선택 버튼
        fileBtn.setOnAction(evt -> handleBrowseFile());
    }

    private void handleBrowseFile() {
        Window window = fileBtn.getScene().getWindow();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("파일 선택");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("모든 파일", "*.*"),
            new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("PDF 파일", "*.pdf")
        );
        java.io.File sel = chooser.showOpenDialog(window);
        if (sel != null) {
            fileField.setText(sel.getAbsolutePath());
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        try {
            AttendanceApprovalRequest req = new AttendanceApprovalRequest();
            req.setReason(reasonArea.getText());
            req.setProofFile(fileField.getText());
            req.setStartDate(startDatePicker.getValue());
            req.setEndDate(endDatePicker.getValue());
            req.setUserId(userId);

            // DAO 인스턴스를 new 로 생성하도록 변경
            AttendanceApprovalRequestDAO dao = new AttendanceApprovalRequestDAO();
            dao.insert(req);

            new Alert(Alert.AlertType.INFORMATION, "신청 완료!").showAndWait();
            // 필요 시 이 창을 닫거나 필드 리셋 로직 추가

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "신청 실패: " + e.getMessage()).showAndWait();
        }
    }

    /**
     * DesktopController에서 전달된 로그인 사용자 정보 세팅
     */
    public void setCurrentUser(User user) {
        if (user == null) {
            System.err.println("[ERROR] ApprovalRequestController.setCurrentUser: user가 null입니다.");
            return;
        }
        this.currentUser = user;
        this.userId      = user.getId();
        System.out.println("[DEBUG] ApprovalRequestController currentUser 설정: "
                           + user.getName() + " (ID: " + userId + ")");
        
    }

    @FXML
    private void handleCloseButton() {
        // 현재 버튼이 속한 윈도우(Stage)를 닫습니다.
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }



    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
