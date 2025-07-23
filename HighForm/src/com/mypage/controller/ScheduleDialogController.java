package com.mypage.controller;

import com.mypage.Model.Schedule;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class ScheduleDialogController {

    @FXML private TextField  titleField;
    @FXML private DatePicker startPicker, endPicker;
    @FXML private TextArea   memoArea;
    @FXML private Button     saveBtn;
    public TextField getTitleField() { return titleField; }
    public TextArea getMemoArea() { return memoArea; }
    public DatePicker getStartPicker() { return startPicker; }
    public DatePicker getEndPicker() { return endPicker; }


    private Schedule result;               // 저장 결과 보관

    /* 저장 버튼 */
    @FXML private void onSave() {
        // 간단 검증
        if (titleField.getText().isBlank()) {
            alert("제목을 입력하세요"); return;
        }
        LocalDate s = startPicker.getValue();
        LocalDate e = endPicker.getValue();
        if (s == null || e == null || e.isBefore(s)) {
            alert("시작~종료 날짜를 올바르게 선택하세요"); return;
        }

        result = new Schedule(
                null,              // id → 시퀀스
                null,              // userId는 호출자가 채움
                titleField.getText().trim(),
                memoArea.getText(),
                s, e
        );
        close();
    }

    /* 취소 버튼 */
    @FXML private void onCancel() { close(); }

    /* 헬퍼 */
    private void close() {
        ((Stage) saveBtn.getScene().getWindow()).close();
    }
    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    /* -------- 호출자에게 결과 전달 -------- */
    public Schedule getResult() { return result; }
}
