package com.mypage.controller;

import com.mypage.Model.attendance.AttendanceApprovalRequest;
import com.mypage.dao.attendance.AttendanceApprovalRequestDAO;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.time.LocalDate;

public class ApprovalRequestController {
    @FXML private TextArea reasonArea;
    @FXML private TextField fileField;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private Button fileBtn;  // ← Browse... 버튼 바인딩

    @FXML
    public void initialize() {
        // "Browse..." 버튼 클릭 시 파일 탐색기 띄우기
        fileBtn.setOnAction(event -> handleBrowseFile());
    }

    // 파일 탐색기 열기
    private void handleBrowseFile() {
        Window window = fileBtn.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("파일 선택");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("모든 파일", "*.*"),
            new FileChooser.ExtensionFilter("이미지 파일", "*.png", "*.jpg", "*.jpeg", "*.gif"),
            new FileChooser.ExtensionFilter("PDF 파일", "*.pdf")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            fileField.setText(selectedFile.getAbsolutePath());
        }
    }

    // 완료 버튼(신청)
    @FXML
    public void handleSubmit(ActionEvent event) {
        try {
            AttendanceApprovalRequest req = new AttendanceApprovalRequest();
            req.setReason(reasonArea.getText());
            req.setProofFile(fileField.getText());

            // LocalDate 직접 전달
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            req.setStartDate(start);
            req.setEndDate(end);

            req.setUserId(1L); // 로그인 구현 전이므로 임시값

            AttendanceApprovalRequestDAO dao = new AttendanceApprovalRequestDAO();
            dao.insert(req);

            new Alert(Alert.AlertType.INFORMATION, "신청 완료!").showAndWait();
            // 창 닫기, 초기화 등 필요한 후처리
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "신청 실패: " + e.getMessage()).showAndWait();
        }
    }
}
