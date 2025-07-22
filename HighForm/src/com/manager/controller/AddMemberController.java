package com.manager.controller;

import java.util.List;

import com.manager.dao.CourseDAO;
import com.manager.dao.MemberDAO;
import com.manager.model.Member;
import com.manager.service.EnrollmentService;
import com.manager.service.MemberService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddMemberController {

	@FXML
	private TextField loginIdField;
	@FXML
	private TextField nameField;
	@FXML
	private TextField phoneField;
	@FXML
	private TextField emailField;
	@FXML
	private ComboBox<String> courseCombo;
	@FXML
	private ComboBox<String> positionCombo;
	@FXML
	private TextArea noteArea;

	@FXML
	private Button registerBtn;

	private CourseDAO courseDAO = new CourseDAO();
	private MemberDAO memberDAO = new MemberDAO();
	private int idx;
	@FXML
	private void initialize() {
		idx = (int)memberDAO.getNextMemberId();
		loadCourses();
		setNextLoginId(idx); // 시퀀스 파생 아이디 출력
		
		List<String> courseList = courseDAO.getOnlyCourse();
		courseCombo.setItems(FXCollections.observableArrayList(courseList));

		registerBtn.setOnAction(e -> handleSubmit());
	}

	private void loadCourses() {
		List<String> courses = courseDAO.getOnlyCourse();

		courseCombo.setItems(FXCollections.observableArrayList(courses));

		// 기본 선택값 설정 (선택 사항)
		if (!courses.isEmpty()) {
			courseCombo.setValue(courses.get(0));
		}
	}

	private void setNextLoginId(int idx) {
		String nextLoginId = "ks" + idx;

		if (nextLoginId != null) {
			loginIdField.setText(nextLoginId);
			loginIdField.setEditable(false); // 수정 불가능하게 설정
		}
	}

	private void handleSubmit() {
		String loginId = loginIdField.getText();
		String password = "1234";
		String name = nameField.getText();
		String phone = phoneField.getText();
		String email = emailField.getText();
		String courseName = courseCombo.getValue();
		String position = positionCombo.getValue();

		// 기본값 유효성 검사
		if (name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty() || email == null
				|| email.trim().isEmpty() || courseName == null || courseName.equals("--") || position == null
				|| position.equals("--")) {
			showAlert("입력 오류", "이름, 연락처, email, 과정명, 직급은 필수 입력사항 입니다.");
			return;
		}

		// 전화번호: 숫자만, 10~11자리
		if (!phone.matches("\\d{10,11}")) {
			showAlert("입력 오류", "연락처는 숫자만 입력하며 10~11자리여야 합니다.");
			return;
		}

		// 이메일: 간단한 이메일 형식 검사
		if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
			showAlert("입력 오류", "이메일 형식이 올바르지 않습니다.");
			return;
		}

		String note = noteArea.getText();

		// Member 객체 생성
		Member member = new Member(idx, loginId, password, name, email, phone, courseName, position);

		MemberService memberService = new MemberService();
		int memberId = memberService.registerMember(member);

		if (memberId > 0) {
			CourseDAO courseDAO = new CourseDAO();
			int courseId = courseDAO.getCourseIdByName(courseName); // 강의명으로 courseId 조회

			if (courseId > 0) {
				EnrollmentService enrollmentService = new EnrollmentService();
				boolean enrolled = enrollmentService.enrollMemberToCourse(memberId, courseId);

				if (enrolled) {
					showAlert("등록 완료", "회원 및 수강 등록이 완료되었습니다.");
					closeWindow();
				} else {
					showAlert("수강 등록 실패", "회원은 등록되었지만 수강 등록에 실패했습니다.");
				}
			} else {
				showAlert("수강 등록 실패", "선택한 강의를 찾을 수 없습니다.");
			}
		} else {
			showAlert("등록 실패", "회원 등록에 실패했습니다.");
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
