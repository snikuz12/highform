package com.mypage.controller;

import com.login.controller.DesktopController;
import com.login.model.User;
import com.mypage.Model.assignment.AssignmentSubmit;
import com.mypage.dao.assignment.AssignmentSubmitDAO;
import com.mypage.dao.assignment.CourseAssignmentDTO;
import com.mypage.dao.assignment.AssignmentSubmitDAO.AssignmentOption;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AssignmentController {

    @FXML private Button submitBtn;  // 메인: 과제 제출 모달
    @FXML private Button listBtn;    // 메인: 등록된 과제 팝업
    @FXML private VBox   assignmentListBox;
    @FXML private HBox   paginationBox;

    @FXML
    private Button closeButton;
    private User currentUser;

    /* (제출 폼 내부) */
    @FXML private ComboBox<AssignmentOption> assignmentCombo;
    @FXML private TextField titleField;
    @FXML private TextArea  contentArea;
    @FXML private TextField fileField;
    @FXML private Button    browseBtn;
    @FXML private Button    submitBtn_form;

    private int currentPage = 1;
    private int pageSize = 10;
    private Long loginUserId = null;     // ← user.getId()로 관리 (Long 타입)
    private boolean isCourseListMode = false;
    private File attachedFile;

    @FXML
    public void initialize() {
        if (submitBtn != null) submitBtn.setOnAction(e -> openSubmitForm());
        if (listBtn != null) listBtn.setOnAction(e -> openRegisteredListWindow());
        if (assignmentListBox != null) {
            isCourseListMode = false;
            loadMySubmitList(currentPage);
        }
        if (closeButton != null) {
            closeButton.setOnAction(e -> {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            });
        }
    }

    /** 로그인 후 사용자 객체를 주입받아 세팅 */
    public void setCurrentUser(User user) {
        if (user == null) {
            System.err.println("[ERROR] AssignmentController.setCurrentUser: user가 null입니다.");
            return;
        }
        this.currentUser = user;
        this.loginUserId = user.getId();  // 핵심: Long 타입의 PK로!
        System.out.println("[DEBUG] AssignmentController currentUser 설정: "
                + user.getName() + " (id: " + loginUserId + ")");

        // currentUser 기준으로 과제 목록 재로딩
        if (isCourseListMode) {
            loadCourseAssignmentList(currentPage);
        } else {
            loadMySubmitList(currentPage);
        }
    }

    /**
     * 닫기 버튼 → Desktop.fxml 로 돌아가며 currentUser 유지
     */
    @FXML
    private void handleCloseButton() {
        try {
            Stage stage = (Stage) closeButton.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/login/Desktop.fxml"));
            Parent root = loader.load();

            DesktopController desktopCtrl = loader.getController();
            if (desktopCtrl != null && currentUser != null) {
                desktopCtrl.setCurrentUser(currentUser);
                System.out.println("[DEBUG] 데스크탑으로 사용자 정보 전달: " + currentUser.getName());
            } else {
                System.err.println("[ERROR] DesktopController 또는 currentUser가 null입니다.");
            }

            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ======= 내 제출 목록 불러오기 =======
    private void loadMySubmitList(int page) {
        try {
            clearListBoxExceptHeader();
            int offset = (page - 1) * pageSize;

            List<AssignmentSubmit> submitList =
                AssignmentSubmitDAO.getInstance().getSubmitList(loginUserId, offset, pageSize);

            int no = offset + 1;
            for (AssignmentSubmit sub : submitList) {
                HBox row = new HBox();
                row.setStyle("-fx-border-color:black; -fx-border-width:0 0 1 0; -fx-alignment:center;");
                row.getChildren().addAll(
                        createTableCell(String.valueOf(no++), 60, false),          // No
                        createTableCell(sub.getAssignmentTitle(), 140, false),     // 과제명
                        createTableCell(sub.getSubmitTitle(), 220, false),         // 제출 제목
                        createTableCell(sub.getContent(), 360, false),             // 내용
                        createTableCell(formatDate(sub.getSubmittedAt()), 160, true)
                );
                assignmentListBox.getChildren().add(row);
            }
            showPagination(page);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ======= 등록된 과제 전체 + 제출여부 =======
    private void loadCourseAssignmentList(int page) {
        try {
            clearListBoxExceptHeader();
            int offset = (page - 1) * pageSize;

            List<CourseAssignmentDTO> list =
                AssignmentSubmitDAO.getInstance()
                        .getCourseAssignmentsWithStatus(loginUserId, offset, pageSize);

            int no = offset + 1;
            for (CourseAssignmentDTO dto : list) {
                HBox row = new HBox();
                row.setStyle("-fx-border-color:black; -fx-border-width:0 0 1 0; -fx-alignment:center;");
                row.getChildren().addAll(
                        createTableCell(String.valueOf(no++), 60, false),
                        createTableCell(dto.getAssignmentTitle(), 300, false),
                        createTableCell(dto.isSubmitted() ? "제출" : "미제출", 120, false),
                        createTableCell(formatDate(dto.getEndDate()), 200, true)
                );
                assignmentListBox.getChildren().add(row);
            }
            showPagination(page);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ======= 등록된 과제 팝업 오픈 =======
    private void openRegisteredListWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/mypage/assignment/RegisteredAssignmentList.fxml"));
            Parent root = loader.load();

            AssignmentController popupCtrl = loader.getController();
            popupCtrl.setLoginUserId(loginUserId);
            popupCtrl.initAsCourseList();

            Stage st = new Stage();
            st.setTitle("등록된 과제");
            st.setScene(new Scene(root));
            st.initModality(Modality.APPLICATION_MODAL);
            st.setResizable(false);
            st.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void setLoginUserId(Long loginUserId) {   // PK(Long)로 세팅
        this.loginUserId = loginUserId;
    }
    public void initAsCourseList() {
        if (assignmentListBox == null) return;
        isCourseListMode = true;
        loadCourseAssignmentList(1);
    }

    private void clearListBoxExceptHeader() {
        if (assignmentListBox.getChildren().size() > 1)
            assignmentListBox.getChildren().remove(1, assignmentListBox.getChildren().size());
    }
    private Label createTableCell(String text, double width, boolean last) {
        Label lbl = new Label(text == null ? "" : text);
        lbl.setMinWidth(width);
        lbl.setPrefWidth(width);
        lbl.setMaxWidth(width);
        lbl.setStyle("-fx-font-size:16px; -fx-alignment:center;" +
                "-fx-border-color:black;" +
                (last ? "-fx-border-width:0;" : "-fx-border-width:0 1 0 0;"));
        return lbl;
    }
    private String formatDate(java.time.LocalDateTime dt) {
        return dt == null ? "" : dt.toString().replace('T', ' ');
    }

    private void showPagination(int selectedPage) {
        paginationBox.getChildren().clear();
        int total = 0;
        try {
            total = isCourseListMode
                    ? AssignmentSubmitDAO.getInstance().getCourseAssignmentCount(loginUserId)
                    : AssignmentSubmitDAO.getInstance().getSubmitCount(loginUserId);
        } catch (Exception ex) { ex.printStackTrace(); }

        int pageCount = (total + pageSize - 1) / pageSize;
        for (int i = 1; i <= pageCount; i++) {
            Button btn = new Button(String.valueOf(i));
            btn.setStyle("-fx-font-size:16px;");
            if (i == selectedPage)
                btn.setStyle("-fx-font-size:16px; -fx-font-weight:bold; "
                        + "-fx-background-color:#00007b; -fx-text-fill:white;");

            int pageNum = i;
            btn.setOnAction(e -> {
                if (isCourseListMode) loadCourseAssignmentList(pageNum);
                else loadMySubmitList(pageNum);
            });
            paginationBox.getChildren().add(btn);
        }
    }

    private void openSubmitForm() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/mypage/assignment/AssignmentSubmit.fxml"));
            Parent root = loader.load();

            ComboBox<AssignmentOption> combo  = (ComboBox<AssignmentOption>) root.lookup("#assignmentCombo");
            TextField  titleTF  = (TextField) root.lookup("#titleField");
            TextArea   contentA = (TextArea)  root.lookup("#contentArea");
            TextField  fileTF   = (TextField) root.lookup("#fileField");
            Button     browseB  = (Button)    root.lookup("#browseBtn");
            Button     submitB  = (Button)    root.lookup("#submitBtn");

            if (combo != null) {
                combo.getItems().setAll(
                        AssignmentSubmitDAO.getInstance().getAvailableAssignmentsForUser(loginUserId));
                combo.setPromptText("과제 선택");
            }
            if (browseB != null && fileTF != null) {
                browseB.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    File f = fc.showOpenDialog(null);
                    if (f != null) {
                        fileTF.setText(f.getName());
                        attachedFile = f;
                    }
                });
            }
            if (submitB != null) submitB.setOnAction(e -> {
                AssignmentOption sel = combo.getValue();
                String title   = titleTF.getText();
                String content = contentA.getText();

                if (sel == null) { msg("과제를 선택하세요."); return; }
                if (title == null || title.isEmpty()) { msg("제목을 입력하세요."); return; }

                AssignmentSubmit sub = new AssignmentSubmit();
                sub.setUserId(loginUserId);
                sub.setAssignmentId(sel.getId());
                sub.setSubmitTitle(title);
                sub.setContent(content);
                sub.setSubmittedAt(java.time.LocalDateTime.now());

                try {
                    AssignmentSubmitDAO.getInstance()
                            .insertWithFile(sub, attachedFile);
                    msg("제출 완료!");
                    submitB.getScene().getWindow().hide();
                    isCourseListMode = false;
                    loadMySubmitList(currentPage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    msg("제출 실패: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });

            Stage st = new Stage();
            st.setTitle("과제 제출");
            st.setScene(new Scene(root));
            st.initModality(Modality.APPLICATION_MODAL);
            st.setResizable(false);
            st.show();

        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void msg(String txt) {
        msg(txt, Alert.AlertType.INFORMATION);
    }
    private void msg(String txt, Alert.AlertType tp) {
        new Alert(tp, txt).showAndWait();
    }
}
