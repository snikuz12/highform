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
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

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
    private final long  loginUserId;
    private final ScheduleDAO scheduleDAO;
    private final LocalDate today = LocalDate.now();

    /* ========== 생성자 ========== */
    public CalendarController(Long loginUserId) {
        this.loginUserId = loginUserId;
        this.scheduleDAO = new ScheduleDaoImpl();
        this.currentYm   = YearMonth.now();
    }

    /* FX 로더용 no‑arg 생성자 (필요시) */
    public CalendarController() {
        this(1L);  // 예시로 1L
    }

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

        monthLabel.setText(currentYm.getYear() + " - " +
            currentYm.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));

        // 요일 헤더 생략...

        List<Schedule> events = fetchSchedules();

        LocalDate first = currentYm.atDay(1);
        int offset = first.getDayOfWeek().getValue() % 7;  // 일=0
        LocalDate cursor = first.minusDays(offset);

        for (int cell = 0; cell < 42; cell++, cursor = cursor.plusDays(1)) {
            // **이 부분이 핵심**: 반복마다 'date'라는 final 변수에 할당
            final LocalDate date = cursor;

            int col = cell % 7, row = cell / 7 + 1;
            StackPane stack = new StackPane();
            stack.getStyleClass().add("day-cell");

            VBox box = new VBox();
            if (date.getMonth().equals(currentYm.getMonth())) {
                box.getStyleClass().add("current-month");
            } else {
                box.getStyleClass().add("other-month");
            }
            if (date.equals(today)) {
                box.getStyleClass().add("today");
            }

            Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
            box.getChildren().add(dayNum);

            // 이제 이 람다 안에서 date 를 안전하게 캡처할 수 있습니다.
            events.stream()
                  .filter(s -> s.contains(date))
                  .forEach(s -> {
                      Label ev = new Label("• " + s.title());
                      ev.getStyleClass().add("event-label");
                      box.getChildren().add(ev);
                  });

            box.setOnMouseClicked(e -> openDailyView(date));
            stack.getChildren().add(box);
            calendarGrid.add(stack, col, row);
        }
    }


    /* ========== DB 조회 ========== */
    private List<Schedule> fetchSchedules() {
        try {
            return scheduleDAO.findByUserAndMonth(loginUserId, currentYm);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return List.of();
        }
    }

    /* ========== 일간 뷰 열기 ========== */
    private void openDailyView(LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/mypage/daily-list.fxml"));
            Parent root = loader.load();

            DailyListController ctrl = loader.getController();
            ctrl.init(date, loginUserId, scheduleDAO, () -> drawCalendar());

            Stage dlg = new Stage();
            dlg.initOwner(calendarGrid.getScene().getWindow());
            dlg.initModality(Modality.WINDOW_MODAL);
            dlg.setTitle("일정 목록: " + date);
            dlg.setScene(new Scene(root));
            dlg.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "목록 표시 오류:\n" + ex.getMessage())
                .showAndWait();
        }
    }

    /* ========== 새 일정 다이얼로그 열기 ========== */
    private void openAddDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/mypage/schedule-dialog.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            dialog.initOwner(addBtn.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("새 일정 등록");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            ScheduleDialogController ctrl = loader.getController();
            Schedule draft = ctrl.getResult();
            if (draft == null) return;

            Schedule full = new Schedule(
                null,
                loginUserId,
                draft.title(),
                draft.memo(),
                draft.startDate(),
                draft.endDate()
            );
            scheduleDAO.save(full);
            drawCalendar();

        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                      "일정 저장 중 오류가 발생했습니다.\n" + ex.getMessage(),
                      ButtonType.OK).showAndWait();
        }
    }
}
