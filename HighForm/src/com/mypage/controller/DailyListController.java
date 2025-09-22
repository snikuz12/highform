package com.mypage.controller;

import com.mypage.Model.Schedule;
import com.mypage.dao.ScheduleDAO;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DailyListController {

    @FXML private Label      dateLabel;
    @FXML private TableView<Schedule> table;
    @FXML private TableColumn<Schedule, String> titleCol, memoCol, periodCol;

    private ScheduleDAO dao;
    private LocalDate   targetDate;
    private Long        loginUserId;

    /* 복사본 업데이트 후 CalendarController가 새로고침하도록 콜백 */
    private Runnable onChanged;         

    /* 초기 세팅 메서드 */
    public void init(LocalDate date, Long userId, ScheduleDAO dao, Runnable onChanged) {
    	System.out.println("### DailyListController.init called ###");
        this.targetDate  = date;
        this.loginUserId = userId;
        this.dao         = dao;
        this.onChanged   = onChanged;

        dateLabel.setText(date.format(DateTimeFormatter.ofPattern("yyyy‑MM‑dd (E)")));

        titleCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().title()));
        memoCol .setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().memo()));
        periodCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(
                c.getValue().startDate().equals(c.getValue().endDate())
                        ? c.getValue().startDate().toString()
                        : c.getValue().startDate() + " ~ " + c.getValue().endDate()
        ));

        refreshTable();
    }

    /* ---------- 버튼 핸들러 ---------- */
    @FXML private void onEdit() {
        Schedule sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        try {
            // 1) 등록 다이얼로그 로드
            FXMLLoader l = new FXMLLoader(getClass().getResource(
                    "/view/mypage/schedule-dialog.fxml"));
            Parent root = l.load();
            ScheduleDialogController dlgCtrl = l.getController();

            // 2) 기존 데이터 세팅
            dlgCtrl.getTitleField().setText(sel.title());
            dlgCtrl.getMemoArea().setText(sel.memo());
            dlgCtrl.getStartPicker().setValue(sel.startDate());
            dlgCtrl.getEndPicker().setValue(sel.endDate());


            // 3) 모달 창
            Stage st = new Stage();
            st.initOwner(table.getScene().getWindow());
            st.initModality(Modality.WINDOW_MODAL);
            st.setTitle("일정 수정");
            st.setScene(new Scene(root));
            st.showAndWait();

            Schedule draft = dlgCtrl.getResult();
            if (draft == null) return; // 수정 취소

            // 4) DB 업데이트
            Schedule updated = new Schedule(
                    sel.id(), loginUserId,
                    draft.title(), draft.memo(),
                    draft.startDate(), draft.endDate()
            );
            dao.update(updated);

            refreshTable();
            onChanged.run();          // ← 캘린더 새로고침

        } catch (Exception ex) {
            ex.printStackTrace();
            alert("수정 실패: " + ex.getMessage());
        }
    }

    @FXML private void onDelete() {
        Schedule sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        if (confirm("삭제하시겠습니까?")) {
            try {
                dao.delete(sel.id());
                refreshTable();
                onChanged.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("삭제 실패: " + ex.getMessage());
            }
        }
    }

    @FXML private void onClose() {
        ((Stage) table.getScene().getWindow()).close();
    }

    /* ---------- 헬퍼 ---------- */
    private void refreshTable() {
        try {
        	System.out.println(">>> refreshTable userId=" + loginUserId);
            /* 1) 전체 일정 조회 */
            List<Schedule> list = dao.findByUser(loginUserId);

            /* 2) 선택 날짜 포함 일정만 필터 */
            ObservableList<Schedule> data = FXCollections.observableArrayList(
                    list.stream()
                        .filter(s -> s.contains(targetDate))
                        .toList()
            );
            table.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
            alert("목록 로드 오류: " + e.getMessage());
        }
    }


    private boolean confirm(String msg) {
        return Alert.AlertType.CONFIRMATION.equals(
                new Alert(Alert.AlertType.CONFIRMATION, msg,
                          ButtonType.OK, ButtonType.CANCEL).showAndWait().orElse(ButtonType.CANCEL))
                && Alert.AlertType.CONFIRMATION != null;
    }
    private void alert(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
