package com.login.controller;

import com.login.model.User;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import com.attendance.service.AttendanceService;
import com.attendance.service.AttendanceCheckResult;
import com.attendance.service.AttendanceCodeService;
import com.attendance.model.Attendance;
import com.attendance.service.exception.AttendanceServiceException;

public class DesktopController {
    
    @FXML private Label welcomeLabel;
    @FXML private Label timeLabel;
    @FXML private Button startButton;
    @FXML private AnchorPane desktopPane;
    
    // 강아지 캐릭터 관련 변수들
    private ImageView dogCharacterView;
    private Label notificationBubble;
    
    private User currentUser;
    private Timeline timeline;
    private Timeline dogAnimationTimeline;
    private Timeline randomNotificationTimeline;
    private Popup startMenu;
    
    // 강아지 애니메이션용 이미지 배열
    private Image[] dogAnimationFrames;
    private Image dogIdleImage; // 기본 고정 이미지
    private int currentAnimationFrame = 0;
    private String currentNotification = "안녕하세요!";
    private boolean isAnimating = false;
    
    private AttendanceService attendanceService;

    public void setAttendanceService(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }
    
    @FXML
    public void initialize() {
        // 시간 업데이트 타이머 시작
        startClock();
        // 시작 메뉴 초기화
        initializeStartMenu();
        // 강아지 캐릭터 초기화 및 데스크톱에 추가
        initializeDogCharacter();
        showDogOnDesktop(); 
        // 랜덤 알림 시작
        startRandomNotifications();
        
     //  오늘의 출석 코드 발급   임시 
        initDailyAttendanceCode();
    }
    
    // 강아지를 데스크톱에 표시
    private void showDogOnDesktop() {
        try {
            // 기본 이미지로 강아지 생성
            dogCharacterView = new ImageView(dogIdleImage);
            dogCharacterView.setFitWidth(250); //가나디 가로
            dogCharacterView.setFitHeight(200);
            dogCharacterView.setLayoutX(600);  // 가나디 좌표
            dogCharacterView.setLayoutY(420);  
            
            // 클릭 이벤트 추가
            dogCharacterView.setOnMouseClicked(e -> onDogCharacterClick());
            
            // 알림 버블 생성
            notificationBubble = new Label(currentNotification);
            notificationBubble.setStyle(
                "-fx-background-color: #ffffcc;" +
                "-fx-border-color: #000000;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 5;" +
                "-fx-font-size: 10px;" +
                "-fx-font-family: 'Malgun Gothic';" +
                "-fx-background-radius: 5;" +
                "-fx-border-radius: 5;"
            );
            notificationBubble.setLayoutX(dogCharacterView.getLayoutX() + 90);
            notificationBubble.setLayoutY(dogCharacterView.getLayoutY() - 20);
            
            // 데스크톱에 추가
            desktopPane.getChildren().addAll(dogCharacterView, notificationBubble);

        } catch (Exception e) {
            System.err.println("강아지 이미지 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 강아지 캐릭터 초기화
    private void initializeDogCharacter() {
        try {
            // 기본 고정 이미지 로드
            dogIdleImage = new Image(getClass().getResourceAsStream("/img2/1.png"));
            
            // 애니메이션 프레임 이미지들 로드 (1.png~20.png)
            dogAnimationFrames = new Image[20];
            for (int i = 0; i < 20; i++) {
                dogAnimationFrames[i] = new Image(getClass().getResourceAsStream("/img2/" + (i + 1) + ".png"));
            }
            
        } catch (Exception e) {
            System.err.println("강아지 캐릭터 이미지를 로드할 수 없습니다: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 강아지 클릭 시 랜덤 애니메이션 재생
    @FXML
    private void onDogCharacterClick() {
        if (isAnimating) {
            return; // 이미 애니메이션 중이면 무시
        }
        
        // 랜덤 응답 메시지
        String[] responses = {
            "안녕하세요! 무엇을 도와드릴까요?",
            "오늘도 화이팅하세요!",
            "잠깐 쉬어가는 것도 좋아요~",
            "궁금한 것이 있으면 말씀하세요!",
            "좋은 하루 되세요!",
            "멍멍! 반가워요!",
            "일하느라 수고 많으세요!",
            "커피 한 잔 어때요?",
            "스트레칭을 해보세요!",
            "오늘 날씨가 좋네요!"
        };
        
        Random random = new Random();
        String response = responses[random.nextInt(responses.length)];
        showNotification(response);
        
        // 랜덤 애니메이션 재생
        playRandomAnimation();
    }
    
    // 랜덤 애니메이션 재생
    private void playRandomAnimation() {
        if (dogAnimationFrames == null || dogAnimationFrames.length == 0) {
            return;
        }
        
        isAnimating = true;
        Random random = new Random();
        
        // 랜덤으로 5-15프레임 재생
        int animationLength = 5 + random.nextInt(11); // 5~15
        int[] randomFrames = new int[animationLength];
        
        // 랜덤 프레임 순서 생성
        for (int i = 0; i < animationLength; i++) {
            randomFrames[i] = random.nextInt(dogAnimationFrames.length);
        }
        
        // 애니메이션 타임라인 생성
        Timeline animationTimeline = new Timeline();
        
        for (int i = 0; i < animationLength; i++) {
            final int frameIndex = randomFrames[i];
            KeyFrame keyFrame = new KeyFrame(
                Duration.millis(i * 200), // 0.2초 간격
                e -> dogCharacterView.setImage(dogAnimationFrames[frameIndex])
            );
            animationTimeline.getKeyFrames().add(keyFrame);
        }
        
        // 애니메이션 종료 후 기본 이미지로 복귀
        KeyFrame finalFrame = new KeyFrame(
            Duration.millis(animationLength * 200),
            e -> {
                dogCharacterView.setImage(dogIdleImage);
                isAnimating = false;
            }
        );
        animationTimeline.getKeyFrames().add(finalFrame);
        
        animationTimeline.play();
    }
    
    // 랜덤 알림 생성
    private void startRandomNotifications() {
        String[] notifications = {
            "뭘보세요?",
            "출석을 확인해주세요~",
            "오늘도 화이팅!",
            "휴가 신청을 해보세요",
            "공지사항을 확인하세요",
            "퇴근 시간이 다가옵니다",
            "안녕하세요!",
            "좋은 하루 되세요!",
            "물 마실 시간이에요!",
            "혹시 , 거북이신가요?",
            "잠시 눈을 쉬어주세요~"
        };
        
        Random random = new Random();
        randomNotificationTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            String randomNotification = notifications[random.nextInt(notifications.length)];
            showNotification(randomNotification);
        }));
        randomNotificationTimeline.setCycleCount(Timeline.INDEFINITE);
        randomNotificationTimeline.play();
    }
    
    // 알림 표시
    public void showNotification(String message) {
        currentNotification = message;
        updateNotificationBubble(message);
        
        // 알림 버블을 4초간 표시하고 기본 메시지로 돌아감
        Timeline hideNotificationTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            updateNotificationBubble("안녕하세요!");
        }));
        hideNotificationTimeline.play();
    }
    
    // 알림 버블 업데이트
    private void updateNotificationBubble(String message) {
        if (notificationBubble != null) {
            notificationBubble.setText(message);
            notificationBubble.setVisible(true);
        }
    }
    
    // 현재 로그인한 사용자 정보 설정
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateWelcomeMessage();
    }
    
    private void updateWelcomeMessage() {
        if (currentUser != null) {
            String roleText = getRoleText(currentUser.getRole());
            welcomeLabel.setText("환영합니다, " + currentUser.getName() + "님! (" + roleText + ")");
        }
    }
    
    private String getRoleText(String role) {
        switch (role) {
            case "STUDENT": return "학생";
            case "PROFESSOR": return "교수";
            case "MANAGER": return "관리자";
            default: return role;
        }
    }
    
    private void startClock() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateTime(); // 즉시 시간 표시
    }
    
    private void updateTime() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        timeLabel.setText(now.format(formatter));
    }
    
    // 이미지 아이콘을 로드하는 헬퍼 메소드
    private ImageView loadIcon(String imageName) {
        try {
            // resources/img2/ 폴더에서 이미지 로드
            Image image = new Image(getClass().getResourceAsStream("/img2/" + imageName));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(30);  // 아이콘 크기 설정
            imageView.setFitHeight(30);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            System.err.println("이미지를 로드할 수 없습니다: " + imageName);
            e.printStackTrace();
            return null;
        }
    }
    
    // 시작 메뉴 초기화
    private void initializeStartMenu() {
        startMenu = new Popup();
        startMenu.setAutoHide(true);
        
        VBox menuBox = new VBox();
        menuBox.setStyle(
            "-fx-background-color: #c0c0c0;" +
            "-fx-border-color: #808080;" +
            "-fx-border-width: 2;" +
            "-fx-padding: 5;" +
            "-fx-spacing: 3;" +
            "-fx-min-width: 300;" +
            "-fx-pref-width: 300;"
        );
        
        // 메뉴 아이템들 
        Button newItem = createMenuItemWithIcon("신청(R)", "application.png");
        newItem.setOnAction(e -> {
            startMenu.hide();
            showNotification("신청 메뉴를 선택했습니다!");
        });
        
        Button helpItem = createMenuItemWithIcon("도움말(H)", "help.png");
        helpItem.setOnAction(e -> {
            startMenu.hide();
            showNotification("도움말을 요청했습니다!");
        });
        
        Button settingsItem = createMenuItemWithIcon("설정(S)", "settings.png");
        settingsItem.setOnAction(e -> {
            startMenu.hide();
            showNotification("설정 메뉴를 열었습니다!");
        });
        
        Button findItem = createMenuItemWithIcon("찾기(F)", "search.png");
        findItem.setOnAction(e -> {
            startMenu.hide();
            showNotification("검색을 시작합니다!");
        });
        
        Separator separator1 = new Separator();
        
        Button logoffItem = createMenuItemWithIcon("administrator 로그오프(L)", "logoff.png");
        logoffItem.setOnAction(e -> {
            startMenu.hide();
            handleLogout();
        });
        
        Button shutdownItem = createMenuItemWithIcon("시스템 종료(U)", "shutdown.png");
        shutdownItem.setOnAction(e -> {
            startMenu.hide();
            handleShutdown();
        });
        
        menuBox.getChildren().addAll(
            newItem, helpItem, settingsItem, findItem, 
            separator1, logoffItem, shutdownItem
        );
        
        startMenu.getContent().add(menuBox);
    }

    private Button createMenuItemWithIcon(String text, String iconFileName) {
        Button button = new Button("  " + text);
        
        // 이미지 아이콘 로드 및 설정
        ImageView icon = loadIcon(iconFileName);
        if (icon != null) {
            button.setGraphic(icon);
        }
        
        button.setStyle(
            "-fx-font-family: 'Malgun Gothic';" +
            "-fx-font-size: 15px;" +
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 8 25 8 15;" +
            "-fx-min-width: 290;" +
            "-fx-pref-width: 290;" +
            "-fx-min-height: 38;" +
            "-fx-pref-height: 38;"
        );
        
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-font-family: 'Malgun Gothic';" +
                "-fx-font-size: 15px;" +
                "-fx-background-color: #000080;" +
                "-fx-text-fill: white;" +
                "-fx-border-color: transparent;" +
                "-fx-alignment: center-left;" +
                "-fx-padding: 8 25 8 15;" +
                "-fx-min-width: 290;" +
                "-fx-pref-width: 290;" +
                "-fx-min-height: 38;" +
                "-fx-pref-height: 38;"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-font-family: 'Malgun Gothic';" +
                "-fx-font-size: 15px;" +
                "-fx-background-color: transparent;" +
                "-fx-text-fill: black;" +
                "-fx-border-color: transparent;" +
                "-fx-alignment: center-left;" +
                "-fx-padding: 8 25 8 15;" +
                "-fx-min-width: 290;" +
                "-fx-pref-width: 290;" +
                "-fx-min-height: 38;" +
                "-fx-pref-height: 38;"
            );
        });
        
        return button;
    }

    
    
    
    @FXML
    private void showStartMenu() {
        if (startMenu.isShowing()) {
            startMenu.hide();
        } else {
            // 시작 버튼의 절대 위치 계산
            Stage stage = (Stage) startButton.getScene().getWindow();
            
            // 시작 버튼의 화면상 절대 좌표 계산
            double buttonX = stage.getX() + startButton.localToScene(0, 0).getX();
            double buttonY = stage.getY() + startButton.localToScene(0, 0).getY();
            
            // 메뉴 높이 (확대된 크기로 계산 - 메뉴 아이템 7개 * 43px + 패딩)
            double menuHeight = 241;
            
            // 시작 버튼 바로 위에 메뉴가 나타나도록 위치 조정
            double x = buttonX;
            double y = buttonY - menuHeight;
            
            // 화면 경계 체크 (메뉴가 화면 위로 벗어나지 않도록)
            if (y < 0) {
                y = buttonY + startButton.getHeight(); // 버튼 아래로 표시
            }
            
            startMenu.show(stage, x, y);
        }
    }
    
    // 바탕화면 아이콘 기능들
    @FXML
    private void openMyComputer() {
        showNotification("내 컴퓨터를 열었습니다!");
    }
    
    @FXML
    private void openFileManager() {
        showNotification("폴더를 열었습니다!");
    }
    
    @FXML
    private void openTrash() {
        showNotification("휴지통을 열었습니다!");
    }
    
    @FXML
    private void openBoard() {
        try {
            showNotification("알림판으로 이동합니다!");
            // 게시판 화면으로 이동
            Stage currentStage = (Stage) startButton.getScene().getWindow();
            Parent board = FXMLLoader.load(getClass().getResource("/view/board/boardList.fxml"));
            currentStage.setScene(new Scene(board, 1000, 750));
            currentStage.setTitle("HighForm - 알림판");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("오류", "알림판을 열 수 없습니다.");
        }
    }
    
    @FXML
    private void openNotice() {
        showNotification("공지사항을 확인해주세요!");
    }
    
    @FXML
    private void openVacationRequest() {
        showNotification("휴가신청서를 작성해보세요!");
    }
    /**
     * 오늘의 출석 코드가 있는지 확인만 함 (생성하지 않음)
     * 스케줄러가 별도로 실행되어야 코드가 존재함
     */
    private void checkTodayAttendanceCode() {
        try {
            AttendanceCodeService service = AttendanceCodeService.getInstance();
            
            // Redis 연결 확인
            if (!service.isRedisConnected()) {
                System.err.println("[앱] Redis 연결 실패 - 출석 코드 조회 불가");
                showNotification("출석 코드 서비스에 연결할 수 없습니다.");
                return;
            }
            
            // 오늘의 코드 조회만 (생성하지 않음)
            String todayCode = service.getTodayCode();
            
            if (todayCode != null) {
                System.out.println("[앱] 오늘의 출석 코드 확인됨: " + todayCode);
                showNotification("오늘의 출석 코드가 준비되었습니다!");
            } else {
                System.out.println("[앱] 오늘의 출석 코드가 없습니다. 스케줄러 실행 상태를 확인하세요.");
                showNotification("출석 코드가 아직 발급되지 않았습니다.");
            }
            
        } catch (Exception e) {
            System.err.println("[앱] 출석 코드 확인 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void openAttendanceCheck() {
        try {
            System.out.println("[DesktopController] openAttendanceCheck 호출됨");
            System.out.println("[DesktopController] currentUser = " + currentUser);

            if (currentUser == null) {
                showAlert("오류", "현재 로그인 정보가 없습니다. 다시 로그인해주세요.");
                return;
            }
            
            // 먼저 오늘의 출석 코드가 있는지 확인
            AttendanceCodeService codeService = AttendanceCodeService.getInstance();
            String todayCode = codeService.getTodayCode();
            
            if (todayCode == null) {
                showAlert("출석 코드 없음", 
                    "오늘의 출석 코드가 발급되지 않았습니다.\n" +
                    "스케줄러가 실행 중인지 확인하거나 관리자에게 문의하세요.");
                return;
            }
            
            // 출석 가능 여부 확인
            AttendanceCheckResult result = attendanceService.canCheckIn(currentUser.getId());
            if (!result.isPossible()) {
                showAlert("출석 체크", result.getMessage());
                return;
            }

            // 출석 코드 입력 받기
            TextInputDialog codeDialog = new TextInputDialog();
            codeDialog.setTitle("출석 코드 입력");
            codeDialog.setHeaderText("출석 코드를 입력하세요");
            codeDialog.setContentText("출석 코드:");

            codeDialog.getDialogPane().setStyle(
                "-fx-background-color: #c0c0c0;" +
                "-fx-font-family: 'Malgun Gothic';" +
                "-fx-font-size: 11px;"
            );

            codeDialog.showAndWait().ifPresent(code -> {
                try {
                    Attendance attendance = attendanceService.checkIn(currentUser.getId(), code);
                    showNotification("출석 완료! 상태: " + attendance.getStatus().getDescription());
                    showAlert("출석 완료", "출석이 완료되었습니다!\n상태: " + attendance.getStatus().getDescription());
                } catch (AttendanceServiceException e) {
                    showAlert("출석 오류", e.getMessage());
                }
            });

        } catch (Exception e) {
            showAlert("출석 오류", "출석 처리 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
    private void showRetroInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle(
            "-fx-background-color: #c0c0c0;" +
            "-fx-font-family: 'Malgun Gothic';" +
            "-fx-font-size: 11px;"
        );
        alert.showAndWait();
    }

    private boolean showRetroConfirmationDialog(String title, String message) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(null);
        confirm.setContentText(message);
        confirm.getDialogPane().setStyle(
            "-fx-background-color: #c0c0c0;" +
            "-fx-font-family: 'Malgun Gothic';" +
            "-fx-font-size: 11px;"
        );
        
        Button yesButton = (Button) confirm.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setText("예");
        Button noButton = (Button) confirm.getDialogPane().lookupButton(ButtonType.CANCEL);
        noButton.setText("아니오");

        return confirm.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
    
    @FXML
    private void openClockOut() {
        try {
            AttendanceCheckResult result = attendanceService.canCheckOut(currentUser.getId());
            if (!result.isPossible()) {
                showRetroInfoDialog("퇴근 체크", result.getMessage());
                return;
            }

            boolean confirmed = showRetroConfirmationDialog("퇴근하기", "진짜 퇴실 처리하시겠습니까?");
            if (confirmed) {
                try {
                    Attendance attendance = attendanceService.checkOut(currentUser.getId());
                    showNotification("퇴근 완료! 근무시간: " + String.format("%.2f", attendance.getWorkingHours()) + "시간");
                    showRetroInfoDialog("퇴근 완료", "수고하셨습니다!\n퇴근 시간: " + attendance.getCheckOut().toLocalTime());
                } catch (AttendanceServiceException e) {
                    showRetroInfoDialog("퇴근 오류", e.getMessage());
                }
            }
        } catch (Exception e) {
            showRetroInfoDialog("퇴근 오류", "퇴근 처리 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
        

    
    @FXML
    private void openCalculator() {
        showNotification("계산기를 실행했습니다!");
    }
    
    private void handleLogout() {
        // 확인 대화상자 표시
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Log Off HighForm");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("정말 로그오프 하시겠습니까?");
        
        // 윈도우 98 스타일 적용
        confirmAlert.getDialogPane().setStyle(
            "-fx-background-color: #c0c0c0;" +
            "-fx-font-family: 'MS Sans Serif';" +
            "-fx-font-size: 11px;"
        );
        
        // 버튼 텍스트 변경
        ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("네");
        ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("아니오");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logout();
            }
        });
    }
    
    private void handleShutdown() {
        Alert shutdownAlert = new Alert(Alert.AlertType.CONFIRMATION);
        shutdownAlert.setTitle("시스템 종료");
        shutdownAlert.setHeaderText(null);
        shutdownAlert.setContentText("정말 프로그램을 종료하시겠습니까?");
        
        shutdownAlert.getDialogPane().setStyle(
            "-fx-background-color: #c0c0c0;" +
            "-fx-font-family: 'Malgun Gothic';" +
            "-fx-font-size: 11px;"
        );
        
        shutdownAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 모든 타임라인 정지
                stopAllTimelines();
                // 프로그램 종료
                Platform.exit();
                System.exit(0);
            }
        });
    }
    
    private void logout() {
        try {
            // 모든 타임라인 정지
            stopAllTimelines();
            
            // 현재 스테이지 가져오기
            Stage currentStage = (Stage) startButton.getScene().getWindow();
            
            // 로그인 FXML 로드
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login/login.fxml"));
            Parent loginPage = loader.load();
            
            // 로그인 화면으로 전환
            Scene loginScene = new Scene(loginPage, 1000, 750);
            currentStage.setScene(loginScene);
            currentStage.setTitle("HighForm Login");
            
            // 현재 사용자 정보 초기화
            currentUser = null;
            
            System.out.println("로그아웃 완료");
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("오류", "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
    
    // 모든 타임라인 정지
    private void stopAllTimelines() {
        if (timeline != null) {
            timeline.stop();
        }
        if (dogAnimationTimeline != null) {
            dogAnimationTimeline.stop();
        }
        if (randomNotificationTimeline != null) {
            randomNotificationTimeline.stop();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // 윈도우 98 스타일로 알럿 창 스타일링
        alert.getDialogPane().setStyle(
            "-fx-background-color: #c0c0c0;" +
            "-fx-font-family: 'Malgun Gothic';" +
            "-fx-font-size: 11px;"
        );
        
        alert.showAndWait();
    }
    
    public void initDailyAttendanceCode() {
        String todayCode = AttendanceCodeService.getInstance().getTodayCode();
        System.out.println("[DEBUG] 오늘 출석 코드 발급됨 = " + todayCode);
    }
}