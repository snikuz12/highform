package com.mypage.controller;

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
import java.util.List;

/**
 * AssignmentController
 *  ─ 메인 화면  : 내 제출 목록 + (과제 제출 버튼)
 *  ─ 팝업 창    : 등록된 과제(수강 중 전체) + 제출 여부 목록
 */
public class AssignmentController {

    /* ───────────────── FXML 주입 컴포넌트 ───────────────── */
    @FXML private Button submitBtn;  // 메인: 과제 제출 모달
    @FXML private Button listBtn;    // 메인: 등록된 과제 팝업
    @FXML private VBox   assignmentListBox;
    @FXML private HBox   paginationBox;

    /* (제출 폼 내부) */
    @FXML private ComboBox<AssignmentOption> assignmentCombo;
    @FXML private TextField titleField;
    @FXML private TextArea  contentArea;
    @FXML private TextField fileField;
    @FXML private Button    browseBtn;
    @FXML private Button    submitBtn_form;

    
    /* ───────── 상태 필드 ───────── */
    private int  currentPage   = 1;
    private int  pageSize      = 10;
    private long loginUserId   = 3L;   // 실제 로그인 사용자 ID 주입 필요
    private boolean isCourseListMode   = false;
    private File attachedFile;  
    

    private AssignmentSubmitDAO assignmentSubmitDAO;
    /* ================================================================
     *  (A) 초기화  (뷰 로드 직후 자동 호출)
     * ============================================================ */
    @FXML
    public void initialize() {
        /* 제출 모달 */
        if (submitBtn != null) submitBtn.setOnAction(e -> openSubmitForm());

        /* ★ 변경 1 : listBtn => 별도 팝업 Stage 로드 */
        if (listBtn != null) listBtn.setOnAction(e -> openRegisteredListWindow());

        /* 메인 화면 첫 진입 : 내 제출 목록 */
        if (assignmentListBox != null) {
            isCourseListMode = false;
            loadMySubmitList(currentPage);
        }
    }

    /* ================================================================
     *  (B) 메인 화면 ① : 내 제출 목록
     * ============================================================ */
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
                	    createTableCell(formatDate(sub.getSubmittedAt()), 160, true) // 제출일
                );
                assignmentListBox.getChildren().add(row);
            }
            showPagination(page);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /* ================================================================
     *  (C) 팝업 화면 ② : 수강중 과제 + 제출 여부 (LEFT JOIN)
     *      ─ 이 메서드는 팝업에서만 호출
     * ============================================================ */
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
                	    createTableCell(formatDate(dto.getEndDate()), 200, true)   // ← 마지막 셀
                	);

                assignmentListBox.getChildren().add(row);
            }

            showPagination(page);
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    /* ================================================================
     *  (D) 등록된 과제 팝업 Stage 오픈
     * ============================================================ */
    private void openRegisteredListWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/mypage/assignment/RegisteredAssignmentList.fxml")); // ★ 팝업용 FXML 경로
            Parent root = loader.load();

            /* 팝업 컨트롤러에 사용자 ID·모드 전달 */
            AssignmentController popupCtrl = loader.getController();
            popupCtrl.setLoginUserId(loginUserId);
            popupCtrl.initAsCourseList();           // 과제 전체 모드로 초기화

            Stage st = new Stage();
            st.setTitle("등록된 과제");
            st.setScene(new Scene(root));
            st.initModality(Modality.APPLICATION_MODAL);
            st.setResizable(false);
            st.show();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    /* ================================================================
     *  (E) 외부에서 모드·사용자 ID 세팅할 때 쓸 public 메서드
     * ============================================================ */
    public void setLoginUserId(long userId) { this.loginUserId = userId; }

    /** 팝업에서 호출: 수강중 과제 모드로 전환 후 첫 페이지 로드 */
    public void initAsCourseList() {
        if (assignmentListBox == null) return; // FXML 주입 완료 전일 수 있음
        isCourseListMode = true;
        loadCourseAssignmentList(1);
    }

    /* ================================================================
     *  (F) 공통 유틸
     * ============================================================ */
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

    /* -------- 페이지네이션 -------- */
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
                else                  loadMySubmitList(pageNum);
            });
            paginationBox.getChildren().add(btn);
        }
    }

    /* ================================================================
     *  (G) 과제 제출 모달 (메인 화면)
     * ============================================================ */
    
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
            

            /* ▼ 1. 과제 선택 콤보 초기화 */
            if (combo != null) {
                combo.getItems().setAll(
                    AssignmentSubmitDAO.getInstance().getAvailableAssignmentsForUser(loginUserId));
                combo.setPromptText("과제 선택");
            }

            /* ▼ 2. 파일 선택 */
            if (browseB != null && fileTF != null) {
                browseB.setOnAction(e -> {
                    FileChooser fc = new FileChooser();
                    File f = fc.showOpenDialog(null);
                    if (f != null) {
                        fileTF.setText(f.getName());
                        attachedFile = f;            // 선택된 파일 보관
                    }
                });
            }

            /* ▼ 3. 제출 버튼 */
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
                    /* ★ 파일 포함 INSERT */
                    AssignmentSubmitDAO.getInstance()
                                       .insertWithFile(sub, attachedFile);
                    msg("제출 완료!");
                    submitB.getScene().getWindow().hide();

                    /* 제출 후 메인 화면 새로고침 */
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

    /** 타입 지정 알림 */
    private void msg(String txt, Alert.AlertType tp) {
        new Alert(tp, txt).showAndWait();
    }
    

}
