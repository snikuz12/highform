package com.manager.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.manager.model.Course;
import com.manager.service.CourseService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.beans.property.SimpleStringProperty;

public class CourseManagementController {

	@FXML
	private TableView<Course> courseTable;

	@FXML
	private TableColumn<Course, String> courseIdColumn; // Integer → String으로 변경

	@FXML
	private TableColumn<Course, String> courseNameColumn;

	@FXML
	private TableColumn<Course, String> startDateColumn;

	@FXML
	private TableColumn<Course, String> endDateColumn;

	@FXML
	private TableColumn<Course, String> instructorColumn;

	@FXML
	private TableColumn<Course, String> managerColumn;

	@FXML
	private TableColumn<Course, String> noteColumn;

	@FXML
	private TextField courseNameField;

	@FXML
	private TextField instructorField;

	@FXML
	private TextField managerField;

	@FXML
	private TextField startDateField;

	@FXML
	private TextField endDateField;

	@FXML
	private TextArea notesArea;

	@FXML
	private Button addBtn;

	@FXML
	private Button editBtn;

	@FXML
	private Button deleteBtn;

	@FXML
	// private Button backButton;

	private CourseService courseService;
	private ObservableList<Course> courseList;

	@FXML
	private void initialize() {
		// 서비스 초기화
		courseService = new CourseService();
		courseList = FXCollections.observableArrayList();

		// 테이블 컬럼 설정
		// 강의ID
		courseIdColumn.setCellValueFactory(cellData -> {
		    Course course = cellData.getValue();
		    if (course != null && course.getCourseId() != 0) { // int -> String 형변환
		        return new SimpleStringProperty(String.valueOf(course.getCourseId()));
		    }
		    return new SimpleStringProperty("");
		});
		// 강의명
		courseNameColumn.setCellValueFactory(cellData -> {
			Course course = cellData.getValue();
			if (course != null) {
				String name = course.getCourseName();
				return new SimpleStringProperty(name != null ? name : "");
			}
			return new SimpleStringProperty("");
		});
		// 강의 시작일
		startDateColumn.setCellValueFactory(cellData -> {
			Course course = cellData.getValue();
			if (course != null) {
				String startDate = course.getStartDate();
				return new SimpleStringProperty(startDate != null ? startDate : "");
			}
			return new SimpleStringProperty("");
		});
		// 강의 종료일
		endDateColumn.setCellValueFactory(cellData -> {
			Course course = cellData.getValue();
			if (course != null) {
				String endDate = course.getEndDate();
				return new SimpleStringProperty(endDate != null ? endDate : "");
			}
			return new SimpleStringProperty("");
		});
		// 강사
		instructorColumn.setCellValueFactory(cellData -> {
			Course course = cellData.getValue();
			if (course != null) {
				String instructor = course.getInstructor();
				return new SimpleStringProperty(instructor != null ? instructor : "");
			}
			return new SimpleStringProperty("");
		});
		// 담당자
		managerColumn.setCellValueFactory(cellData -> {
			Course course = cellData.getValue();
			if (course != null) {
				String manager = course.getManager();
				return new SimpleStringProperty(manager != null ? manager : "");
			}
			return new SimpleStringProperty("");
		});
		// 비고
		noteColumn.setCellValueFactory(cellData -> {
			Course course = cellData.getValue();
			if (course != null) {
				String note = course.getNote();
				return new SimpleStringProperty(note != null ? note : "");
			}
			return new SimpleStringProperty("");
		});

		// 컬럼 폭 설정
		courseIdColumn.setPrefWidth(30);
		courseNameColumn.setPrefWidth(180);
		startDateColumn.setPrefWidth(100);
		endDateColumn.setPrefWidth(100);
		instructorColumn.setPrefWidth(120);
		managerColumn.setPrefWidth(100);
		noteColumn.setPrefWidth(150);

		// 컬럼 크기 조정 정책 설정
		courseTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// 테이블에 데이터 바인딩
		courseTable.setItems(courseList);

		// 초기 데이터 로드
		loadCourseData();

		// 테이블 선택 이벤트 핸들러
		courseTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				populateFields(newValue);
			}
		});

		System.out.println("CourseManagementController 초기화 완료");
	}

	// 데이터 불러오기
	private void loadCourseData() {
		try {
			courseList.clear();
			List<Course> courses = courseService.getAllCourses();

			// 데이터 확인용 디버깅
			for (Course course : courses) {
				System.out.println("로드된 강의: " + course.getCourseName() + " | " + course.getInstructor());
			}

			courseList.addAll(courses);
			courseTable.refresh();

			System.out.println("강의 데이터 로드 완료: " + courseList.size() + "건");

		} catch (Exception e) {
			System.err.println("데이터 로드 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 선택한 데이터를 (하단) 필드에 채우기
	private void populateFields(Course course) {
		courseNameField.setText(course.getCourseName());
		startDateField.setText(course.getStartDate());
		endDateField.setText(course.getEndDate());
		instructorField.setText(course.getInstructor());
		managerField.setText(course.getManager());
		notesArea.setText(course.getNote());
	}

	private void clearFields() {
		courseNameField.clear();
		startDateField.clear();
		endDateField.clear();
		instructorField.clear();
		managerField.clear();
		notesArea.clear();
		courseTable.getSelectionModel().clearSelection();
	}
	// 강의 추가
	@FXML
	private void handleAddCourse(ActionEvent event) {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/manager/addCourse.fxml"));
	        Parent root = loader.load();

	        Stage popupStage = new Stage();
	        popupStage.setTitle("강의 추가");
	        popupStage.initModality(Modality.APPLICATION_MODAL); // 부모 창 잠금
	        popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
	        popupStage.setScene(new Scene(root));
	        popupStage.showAndWait(); // 모달창, 닫을 때까지 대기

	        // 팝업 닫힌 후 데이터 다시 로드 (필요시)
	        loadCourseData();

	    } catch (IOException e) {
	        e.printStackTrace();
	        showAlert("오류", "강의 추가 화면을 불러오는 데 실패했습니다.");
	    }
	}

	// 강의 수정
	@FXML	
	private void handleEditCourse() {
		Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();

	    if (selectedCourse == null) {
	    	showAlert("선택 오류", "수정할 강의를 선택하세요.");
	        return;
	    }

	    // 1. 수정 전 확인 다이얼로그
	    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
	    confirmAlert.setTitle("수정 확인");
	    confirmAlert.setHeaderText("선택한 강의를 수정하시겠습니까?");
	    confirmAlert.setContentText(selectedCourse.getCourseName());

	    // 2. 확인/취소 버튼 대기
	    Optional<ButtonType> result = confirmAlert.showAndWait();

	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        // 3. 사용자가 '확인'을 누르면 수정 진행
	        boolean success = courseService.updateCourse(selectedCourse);
	        
	        
	        if (success) {
	    		selectedCourse.setCourseName(courseNameField.getText());
	    		selectedCourse.setStartDate(startDateField.getText());
	    		selectedCourse.setEndDate(endDateField.getText());
	    		selectedCourse.setInstructor(instructorField.getText());
	    		selectedCourse.setManager(managerField.getText());
	    		selectedCourse.setNote(notesArea.getText());
	    
	    		courseService.updateCourse(selectedCourse);
	    		
	            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "강의가 수정되었습니다.");
	            successAlert.show();
	        } else {
	            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "강의 수정 중 오류가 발생했습니다.");
	            errorAlert.show();
	        }
	        loadCourseData();
	        clearFields();
	    } else {
	        System.out.println("강의 수정이 취소되었습니다.");
	    }
	}
	

	// 강의 삭제
	@FXML
	private void handleDeleteCourse() {
		Course selectedCourse = courseTable.getSelectionModel().getSelectedItem();

	    if (selectedCourse == null) {
	    	showAlert("선택 오류", "삭제할 강의를 선택하세요.");
	        return;
	    }

	    // 1. 삭제 전 확인 다이얼로그
	    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
	    confirmAlert.setTitle("삭제 확인");
	    confirmAlert.setHeaderText("선택한 강의를 삭제하시겠습니까?");
	    confirmAlert.setContentText(selectedCourse.getCourseName());

	    // 2. 확인/취소 버튼 대기
	    Optional<ButtonType> result = confirmAlert.showAndWait();

	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        // 3. 사용자가 '확인'을 누르면 삭제 진행
	        boolean success = courseService.deleteCourse(selectedCourse.getCourseId());
	        
	        if (success) {				
	            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "강의가 삭제되었습니다.");
	            successAlert.show();
	        } else {
	            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "강의 삭제 중 오류가 발생했습니다.");
	            errorAlert.show();
	        }
	        loadCourseData();
	        clearFields();
	    } else {
	        System.out.println("강의 삭제가 취소되었습니다.");
	    }
	}

	// 경고창 출력 유틸
	private void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
	
	// 이전 화면으로 돌아가기
	@FXML
	private void handleBackMenu(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/manager/menuSelect.fxml"));
			Parent root = loader.load();

			// 현재 이벤트가 발생한 노드에서 Stage 가져오기
			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

			// 해당 Stage의 Scene을 바꿈
			stage.setScene(new Scene(root));
			stage.setTitle("Management System"); // 타이틀 설정 (선택)
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert("오류", "이전 화면으로 돌아가는 데 실패했습니다.");
		}
	}

}