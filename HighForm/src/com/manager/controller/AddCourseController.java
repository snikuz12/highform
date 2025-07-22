package com.manager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.manager.dao.CourseDAO;
import com.manager.model.Course;

import java.time.format.DateTimeFormatter;

public class AddCourseController {

    @FXML private TextField courseNameField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField instructorField;
    @FXML private TextField managerField;
    @FXML private TextArea noteArea;
    @FXML private Button registerBtn;

    private CourseDAO courseDAO = new CourseDAO();  // DAO 인스턴스

    @FXML
    private void initialize() {
        // 초기화 시 필요 작업
    	registerBtn.setOnAction(e -> handleSubmit());
    }

    private void handleSubmit() {
        String courseName = courseNameField.getText();
        String instructor = instructorField.getText();

        if (courseName == null || courseName.isEmpty() ||
            instructor == null || instructor.isEmpty()) {
            showAlert("입력 오류", "강의명과 교수는 필수 입력입니다.");
            return;
        }

        String startDate = (startDatePicker.getValue() != null)
                ? startDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null;

        String endDate = (endDatePicker.getValue() != null)
                ? endDatePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null;
        String manager = managerField.getText();
        String note = noteArea.getText();

        // Course 객체 생성 (id는 시퀀스로 처리한다고 가정해서 0)
        Course course = new Course(0, courseName, startDate, endDate, instructor, manager, note);

        boolean success = courseDAO.addCourse(course);
        if (success) {
            showAlert("등록 완료", "강의가 성공적으로 등록되었습니다.");
            closeWindow();
        } else {
            showAlert("등록 실패", "DB 등록 중 문제가 발생했습니다.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) registerBtn.getScene().getWindow();
        stage.close();
    }
}