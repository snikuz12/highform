package com.mypage.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.mypage.Model.Schedule;
import com.mypage.dao.ScheduleDAO;
import com.mypage.dao.ScheduleDaoImpl;
import com.mypage.controller.DailyListController;
import com.mypage.controller.ScheduleDialogController;

public class CalendarController implements Initializable {

    /* ========== FXML 필드 ========== */
    @FXML private Label   monthLabel;
    @FXML private Button  prevBtn, nextBtn, addBtn, editBtn;
    @FXML private GridPane calendarGrid;

    /* ========== 내부 상태 ========== */
    private YearMonth        currentYm;
    private final long       loginUserId;
    private final ScheduleDAO scheduleDAO;
    private final LocalDate   today = LocalDate.now();

    /* ========== 생성자 ========== */
    public CalendarController(Long loginUserId) {
        this.loginUserId = loginUserId;
        this.scheduleDAO = new ScheduleDaoImpl();
        this.currentYm   = YearMonth.now();
    }
    /** FX용 no‑arg 생성자 */
    public CalendarController() {
        this(1L);
    }

    /* ========== 초기화 콜백 ========== */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 그리드가 VBox 남은 공간을 모두 차지하도록 설정
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);
        calendarGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // 버튼 이벤트 바인딩
        prevBtn.setOnAction(e -> moveMonth(-1));
        nextBtn.setOnAction(e -> moveMonth(+1));
        addBtn .setOnAction(e -> openAddDialog());
        editBtn.setOnAction(e -> openDailyView(LocalDate.now()));

        // 최초 달력 그리기
        drawCalendar();
    }

    /* ========== 월 이동 ========== */
    private void moveMonth(int offset) {
        currentYm = currentYm.plusMonths(offset);
        drawCalendar();
    }

    /* ========== 달력 렌더링 ========== */
    private void drawCalendar() {
        // 기존 컨텐츠 초기화
        calendarGrid.getChildren().clear();

        // 1) 요일 헤더 (0행)
        String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        for (int col = 0; col < 7; col++) {
            Label hdr = new Label(days[col]);
            hdr.getStyleClass().add("month-label");
            StackPane cell = new StackPane(hdr);
            cell.getStyleClass().add("header-cell");
            calendarGrid.add(cell, col, 0);
        }

        // 2) 상단 월 레이블 갱신
        monthLabel.setText(
            currentYm.getYear() + " - " +
            currentYm.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
        );

        // 3) DB에서 일정 불러오기
        List<Schedule> events = fetchSchedules();

        // 4) 첫 날짜(1일 이전 주 일요일) 계산
        LocalDate firstOfMonth = currentYm.atDay(1);
        int offset = firstOfMonth.getDayOfWeek().getValue() % 7;  // 일=0
        LocalDate cursor = firstOfMonth.minusDays(offset);

        // 5) 날짜 셀(1~6행, 6주×7일)
        for (int i = 0; i < 42; i++, cursor = cursor.plusDays(1)) {
            final LocalDate date = cursor;
            int col = i % 7;
            int row = i / 7 + 1;

            StackPane stack = new StackPane();
            stack.getStyleClass().add("day-cell");
            if (!date.getMonth().equals(currentYm.getMonth())) {
                stack.getStyleClass().add("other-month");
            }
            if (date.equals(today)) {
                stack.getStyleClass().add("today");
            }

            VBox box = new VBox();
            Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
            box.getChildren().add(dayNum);

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

    /* ========== DB에서 일정 가져오기 ========== */
    private List<Schedule> fetchSchedules() {
        try {
            return scheduleDAO.findByUserAndMonth(loginUserId, currentYm);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return List.of();
        }
    }

    /* ========== 일간 뷰 팝업 열기 ========== */
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
            new Alert(Alert.AlertType.ERROR,
                      "목록 표시 오류:\n" + ex.getMessage(),
                      ButtonType.OK).showAndWait();
        }
    }

    /* ========== 새 일정 팝업 열기 ========== */
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
