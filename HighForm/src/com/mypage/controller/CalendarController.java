package com.mypage.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.ResourceBundle;

import com.mypage.Model.Schedule;
import com.mypage.dao.ScheduleDAO;
import com.mypage.dao.ScheduleDaoImpl;         


public class CalendarController implements Initializable {

    /* ========== FXML ========== */
    @FXML private Label  monthLabel;
    @FXML private Button prevBtn, nextBtn, addBtn, editBtn;
    @FXML private GridPane calendarGrid;

    /* ========== 상태 ========== */
    private YearMonth   currentYm;
    private Long loginUserId;
    private final ScheduleDAO scheduleDAO;

    /* ========== 생성자 ========== */
    public CalendarController(Long loginUserId) {
        this.loginUserId = loginUserId;
        this.scheduleDAO = new ScheduleDaoImpl();   // ⬅️ ④ DBConnection 사용 DAO
        this.currentYm   = YearMonth.now();
    }

    /* FXML 로더가 no‑arg 생성자를 요구한다면 ↓ 하나 더 추가해두면 됩니다.
    public CalendarController() {
        this(SessionContext.getLoginUserId());      // 직접 관리하는 세션 헬퍼
    }
    */

    /* ========== 초기화 ========== */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        prevBtn.setOnAction(e -> moveMonth(-1));
        nextBtn.setOnAction(e -> moveMonth(+1));
        addBtn .setOnAction(e -> openAddDialog());
        editBtn.setOnAction(e -> openDailyView(LocalDate.now()));
        drawCalendar();
    }

    /* ========== 달 이동 ========== */
    private void moveMonth(int offset) {
        currentYm = currentYm.plusMonths(offset);
        drawCalendar();
    }

    /* ========== 달력 그리기 ========== */
    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        monthLabel.setText(currentYm.toString());   // ex) 2025‑07

        LocalDate first = currentYm.atDay(1);
        int startCol = first.getDayOfWeek().getValue() % 7; // 일요일=0

        List<Schedule> events = fetchSchedules();           // 월간 스케줄

        LocalDate cursor = first.minusDays(startCol);
        for (int cell = 0; cell < 42; cell++, cursor = cursor.plusDays(1)) {
            StackPane dayCell = createDayCell(cursor, events);
            calendarGrid.add(dayCell, cell % 7, cell / 7);
        }
    }

    /* ========== DB 조회 ========== */
    private List<Schedule> fetchSchedules() {
        try {
            return scheduleDAO.findByUserAndMonth(loginUserId, currentYm);
        } catch (SQLException ex) {
            ex.printStackTrace();   // TODO: 로깅 / 알림
            return List.of();
        }
    }

    /* ========== 셀 구성 ========== */
    private StackPane createDayCell(LocalDate date, List<Schedule> events) {
        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        VBox  box    = new VBox(dayNum);
        box.getStyleClass().add(date.getMonth().equals(currentYm.getMonth())
                                ? "current-month" : "other-month");

        events.stream()
              .filter(s -> s.contains(date))
              .forEach(s -> box.getChildren().add(new Label("• " + s.title())));

        box.setOnMouseClicked(e -> openDailyView(date));
        return new StackPane(box);
    }


    private void openDailyView(LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/view/mypage/daily-list.fxml"));
            Parent root = loader.load();

            DailyListController ctrl = loader.getController();
            ctrl.init(date, loginUserId, scheduleDAO, this::drawCalendar);

            Stage dlg = new Stage();
            dlg.initOwner(calendarGrid.getScene().getWindow());
            dlg.initModality(Modality.WINDOW_MODAL);
            dlg.setTitle("일정 목록: " + date);
            dlg.setScene(new Scene(root));
            dlg.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "목록 표시 오류:\n" + ex.getMessage()).showAndWait();
        }
    }

    
    private void openAddDialog() {
        try {
            // 1) 다이얼로그 FXML 로드
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/mypage/schedule-dialog.fxml"));
            Parent root = loader.load();

            // 2) 모달 Stage 구성
            Stage dialog = new Stage();
            dialog.initOwner(addBtn.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("새 일정 등록");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            // 3) 결과 확인
            ScheduleDialogController ctrl = loader.getController();
            Schedule draft = ctrl.getResult();
            if (draft == null) return;               // 사용자가 취소

            // 4) userId 채우고 저장
            Schedule full = new Schedule(
                    null,
                    loginUserId,
                    draft.title(),
                    draft.memo(),
                    draft.startDate(),
                    draft.endDate()
            );
            scheduleDAO.save(full);

            // 5) 달력 새로고침
            drawCalendar();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                      "일정 저장 중 오류가 발생했습니다.\n" + ex.getMessage(),
                      ButtonType.OK).showAndWait();
        }
    }

}
