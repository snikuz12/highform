package com.manager.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.manager.model.Course;
import com.manager.model.Member;
import com.manager.service.MemberService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MemberManagementController {
	@FXML
	private TableView<Member> memberTable;

	@FXML
	private TableColumn<Member, String> idColumn; // Integer → String으로 변경

	@FXML
	private TableColumn<Member, String> loginIdColumn;

	@FXML
	private TableColumn<Member, String> nameColumn;

	@FXML
	private TableColumn<Member, String> emailColumn;

	@FXML
	private TableColumn<Member, String> phoneColumn;

	@FXML
	private TableColumn<Member, String> affiliationColumn; // 소속

	@FXML
	private TableColumn<Member, String> positionColumn;

	@FXML
	private TextField idField;

	@FXML
	private TextField nameField;

	@FXML
	private TextField positionField;

	@FXML
	private TextField phoneField;

	@FXML
	private TextField emailField;

	@FXML
	private TextField affiliationField;

	@FXML
	private TextArea notesArea;

	@FXML
	private Button newMemberBtn;

	@FXML
	private Button deleteBtn;

	@FXML
	private Button editBtn;

	@FXML
	private Button backButton;

	private MemberService memberService;
	private ObservableList<Member> memberList;

	@FXML
	private void initialize() {
		// 서비스 초기화
		memberService = new MemberService();
		memberList = FXCollections.observableArrayList();

		// 테이블 컬럼 설정
		// 인덱스 ID
		idColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null && member.getMemberId() != 0) { // int -> String 형변환
				return new SimpleStringProperty(String.valueOf(member.getMemberId()));
			}
			return new SimpleStringProperty("");
		});
		// ID
		loginIdColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null) {
				String loginId = member.getMemberLoginId();
				return new SimpleStringProperty(loginId != null ? loginId : "");
			}
			return new SimpleStringProperty("");
		});
		// 이름
		nameColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null) {
				String name = member.getMemberName();
				return new SimpleStringProperty(name != null ? name : "");
			}
			return new SimpleStringProperty("");
		});
		// 메일
		emailColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null) {
				String email = member.getEmail();
				return new SimpleStringProperty(email != null ? email : "");
			}
			return new SimpleStringProperty("");
		});
		// 연락처
		phoneColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null) {
				String phone = member.getPhoneNumber();
				return new SimpleStringProperty(phone != null ? phone : "");
			}
			return new SimpleStringProperty("");
		});
		// 소속
		affiliationColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null) {
				String affiliation = member.getAffiliation();
				return new SimpleStringProperty(affiliation != null ? affiliation : "");
			}
			return new SimpleStringProperty("");
		});
		// 직급
		positionColumn.setCellValueFactory(cellData -> {
			Member member = cellData.getValue();
			if (member != null) {
				String position = member.getPosition();
				return new SimpleStringProperty(position != null ? position : "");
			}
			return new SimpleStringProperty("");
		});

		// 컬럼 폭 설정
		idColumn.setPrefWidth(40);
		loginIdColumn.setPrefWidth(100);
		nameColumn.setPrefWidth(80);
		emailColumn.setPrefWidth(150);
		phoneColumn.setPrefWidth(80);
		affiliationColumn.setPrefWidth(150);
		positionColumn.setPrefWidth(80);

		// 컬럼 크기 조정 정책 설정
		memberTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

		// 테이블에 데이터 바인딩
		memberTable.setItems(memberList);

		// Todo 수강 강의정보 조인 필요 - 추후에 수정

		// 초기 데이터 로드
		loadMemberData();

		// 테이블 선택 이벤트 핸들러
		memberTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				populateFields(newValue);
			}
		});

		System.out.println("MemberManagementController 초기화 완료");
	}

	// 데이터 불러오기
	private void loadMemberData() {
		try {
			memberList.clear();
			List<Member> members = memberService.getAvailableMembers();

			// 데이터 확인용 디버깅
			for (Member member : members) {
				System.out.println("로드된 유저: " + member.getMemberName() + " | " + member.getAffiliation());
			}

			memberList.addAll(members);
			memberTable.refresh();

			System.out.println("강의 데이터 로드 완료: " + memberList.size() + "건");

		} catch (Exception e) {
			System.err.println("데이터 로드 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// 선택한 데이터를 (하단) 필드에 채우기
	private void populateFields(Member member) {
		idField.setText(member.getMemberLoginId());
		nameField.setText(member.getMemberName());
		positionField.setText(member.getPosition());
		phoneField.setText(member.getPhoneNumber());
		emailField.setText(member.getEmail());
		affiliationField.setText(member.getAffiliation());
		notesArea.setText("");
	}

	private void clearFields() {
		idField.clear();
		nameField.clear();
		positionField.clear();
		phoneField.clear();
		emailField.clear();
		affiliationField.clear();
		notesArea.clear();
		memberTable.getSelectionModel().clearSelection();
	}

	// 회원 추가
	@FXML
	private void handleAddCourse(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/manager/memberRegistration.fxml"));
			Parent root = loader.load();

			Stage popupStage = new Stage();
			popupStage.setTitle("회원 추가");
			popupStage.initModality(Modality.APPLICATION_MODAL); // 부모 창 잠금
			popupStage.initOwner(((Node) event.getSource()).getScene().getWindow());
			popupStage.setScene(new Scene(root));
			popupStage.showAndWait(); // 모달창, 닫을 때까지 대기

			// 팝업 닫힌 후 데이터 다시 로드 (필요시)
			loadMemberData();

		} catch (IOException e) {
			e.printStackTrace();
			showAlert("오류", "회원 등록 화면을 불러오는 데 실패했습니다.");
		}
	}

	// 회원 수정
	@FXML
	private void handleEditCourse() {
		Member selectedMember = memberTable.getSelectionModel().getSelectedItem();

		if (selectedMember == null) {
			showAlert("선택 오류", "수정할 회원을 선택하세요.");
			return;
		}

		// 1. 수정 전 확인 다이얼로그
		Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
		confirmAlert.setTitle("수정 확인");
		confirmAlert.setHeaderText("선택한 회원을 수정하시겠습니까?");
		confirmAlert.setContentText(selectedMember.getMemberName());

		// 2. 확인/취소 버튼 대기
		Optional<ButtonType> result = confirmAlert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			// 3. 사용자가 '확인'을 누르면 수정 진행
			boolean success = memberService.updateMember(selectedMember);

			if (success) {
				selectedMember.setMemberName(nameField.getText());
				selectedMember.setPosition(positionField.getText());
				selectedMember.setPhoneNumber(phoneField.getText());
				selectedMember.setEmail(emailField.getText());
				selectedMember.setAffiliation(affiliationField.getText());

				memberService.updateMember(selectedMember);

				Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "회원 정보가 수정되었습니다.");
				successAlert.show();
			} else {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR, "회원 정보 수정 중 오류가 발생했습니다.");
				errorAlert.show();
			}
			loadMemberData();
			clearFields();
		} else {
			System.out.println("회원 정보 수정이 취소되었습니다.");
		}
	}

	// 회원 삭제
	@FXML
	private void handleDeleteCourse() {
		Member selectedMember = memberTable.getSelectionModel().getSelectedItem();

		if (selectedMember == null) {
			showAlert("선택 오류", "삭제할 회원을 선택하세요.");
			return;
		}

		// 1. 삭제 전 확인 다이얼로그
		Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
		confirmAlert.setTitle("삭제 확인");
		confirmAlert.setHeaderText("선택한 회원을 삭제하시겠습니까?");
		confirmAlert.setContentText(selectedMember.getMemberName());

		// 2. 확인/취소 버튼 대기
		Optional<ButtonType> result = confirmAlert.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			// 3. 사용자가 '확인'을 누르면 삭제 진행
			boolean success = memberService.deleteMember(selectedMember.getMemberId());

			if (success) {
				Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "회원이 삭제되었습니다.");
				successAlert.show();
			} else {
				Alert errorAlert = new Alert(Alert.AlertType.ERROR, "회원 삭제 중 오류가 발생했습니다.");
				errorAlert.show();
			}
			loadMemberData();
			clearFields();
		} else {
			System.out.println("회원 삭제가 취소되었습니다.");
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
