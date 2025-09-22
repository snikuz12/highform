package com.mypage.controller;

import com.login.controller.DesktopController;
import com.login.model.User;
import com.mypage.Model.Schedule;
import com.mypage.dao.ScheduleDAO;
import com.mypage.dao.ScheduleDaoImpl;

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

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CalendarController implements Initializable {

    /* ========== FXML 필드 ========== */
    @FXML private Label   monthLabel;
    @FXML private Button  prevBtn, nextBtn, addBtn, editBtn, closeButton,closeBtn;
    @FXML private GridPane calendarGrid;

    /* ========== 상태 필드 ========== */
    private YearMonth     currentYm;
    private long          loginUserId;
    private final ScheduleDAO scheduleDAO;
    private final LocalDate   today = LocalDate.now();
    private User          currentUser;

    /* ========== 생성자 ========== */
    public CalendarController(Long loginUserId) {
        this.loginUserId = loginUserId;
        this.scheduleDAO = new ScheduleDaoImpl();
        this.currentYm   = YearMonth.now();
    }
    /** FX용 no‑arg 생성자 (초기 사용자 ID를 1L로 설정) */
    public CalendarController() {
        this(1L);
    }

    /* ========== 초기화 콜백 ========== */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // GridPane이 VBox 남은 공간을 채우도록
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);
        calendarGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // 버튼 이벤트 바인딩
        prevBtn   .setOnAction(e -> moveMonth(-1));
        nextBtn   .setOnAction(e -> moveMonth(+1));
        addBtn    .setOnAction(e -> openAddDialog());
        editBtn   .setOnAction(e -> openDailyView(LocalDate.now()));
        closeButton.setOnAction(e -> handleCloseButton());

        // 최초 달력 그리기
        drawCalendar();
    }

    /* ========== 사용자 정보 설정 ========== */
    /**
     * DesktopController 등에서 로그인된 User 전달 시 호출
     */
    public void setCurrentUser(User user) {
        if (user == null) {
            System.err.println("[ERROR] CalendarController.setCurrentUser: user가 null입니다.");
            return;
        }
        this.currentUser = user;
        this.loginUserId = user.getId();
        System.out.println("[CalendarController] 로그인 사용자: "
            + user.getName() + " (ID: " + loginUserId + ")");
        // 사용자 변경 시 달력 다시 그리기
        drawCalendar();
    }

    /* ========== 창 닫기 ========== */
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
            }
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                      "데스크탑으로 복귀 중 오류가 발생했습니다.\n" + ex.getMessage(),
                      ButtonType.OK).showAndWait();
        }
    }

    /* ========== 월 이동 ========== */
    private void moveMonth(int offset) {
        currentYm = currentYm.plusMonths(offset);
        drawCalendar();
    }

    /* ========== 달력 그리기 ========== */
    private void drawCalendar() {
        calendarGrid.getChildren().clear();

        // 요일 헤더
        String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
        for (int col = 0; col < 7; col++) {
            Label hdr = new Label(days[col]);
            hdr.getStyleClass().add("month-label");
            StackPane cell = new StackPane(hdr);
            cell.getStyleClass().add("header-cell");
            calendarGrid.add(cell, col, 0);
        }

        // 월 레이블
        monthLabel.setText(
            currentYm.getYear() + " - " +
            currentYm.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
        );

        // 일정 조회
        List<Schedule> events = fetchSchedules();

        // 시작 날짜 계산
        LocalDate first = currentYm.atDay(1);
        int offset = first.getDayOfWeek().getValue() % 7;
        LocalDate date = first.minusDays(offset);

        // 날짜 셀
        for (int i = 0; i < 42; i++, date = date.plusDays(1)) {
            final LocalDate d = date;
            int col = i % 7, row = i / 7 + 1;

            StackPane stack = new StackPane();
            stack.getStyleClass().add("day-cell");
            if (!d.getMonth().equals(currentYm.getMonth())) {
                stack.getStyleClass().add("other-month");
            }
            if (d.equals(today)) {
                stack.getStyleClass().add("today");
            }

            VBox box = new VBox();
            box.getChildren().add(new Label(String.valueOf(d.getDayOfMonth())));
            events.stream()
                  .filter(ev -> ev.contains(d))
                  .forEach(ev -> {
                      Label lbl = new Label("• " + ev.title());
                      lbl.getStyleClass().add("event-label");
                      box.getChildren().add(lbl);
                  });
            box.setOnMouseClicked(e -> openDailyView(d));
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

    /* ========== 새 일정 열기 ========== */
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
                      "일정 저장 중 오류:\n" + ex.getMessage(),
                      ButtonType.OK).showAndWait();
        }
    }
    
    @FXML
    private void onClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
