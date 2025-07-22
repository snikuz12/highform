package com;
import java.sql.SQLException;
import com.board.dao.BoardDao;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showSplash();
    }

    private void showSplash() throws Exception {
        Parent splash = FXMLLoader.load(getClass().getResource("/view/login/splash.fxml"));
        primaryStage.setScene(new Scene(splash, 1000, 750));
        primaryStage.show();

        // 3초 후 로딩 화면으로 전환
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            Platform.runLater(() -> {
                try { showLoading(); } catch (Exception e) { e.printStackTrace(); }
            });
        }).start();
    }

    private void showLoading() throws Exception {
        Parent loading = FXMLLoader.load(getClass().getResource("/view/login/loading.fxml"));
        primaryStage.setScene(new Scene(loading, 1000, 750));

        // 5초 후 로그인 화면으로 전환
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            Platform.runLater(() -> {
                try { showLogin(); } catch (Exception e) { e.printStackTrace(); }
            });
        }).start();
    }

    private void showLogin() throws Exception {
    	Parent splash = FXMLLoader.load(getClass().getResource("/view/login/login.fxml"));
    	Scene scene = new Scene(splash, 1000, 750);
    	scene.getStylesheets().add(getClass().getResource("/fonts/global.css").toExternalForm());
    	primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}