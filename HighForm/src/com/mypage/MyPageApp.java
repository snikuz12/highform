package com.mypage;

import com.mypage.controller.CalendarController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyPageApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mypage/assignment/my_assignment_list.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mypage/attendance/attendance_list.fxml"));
        loader.setControllerFactory(type -> {                
            if (type == CalendarController.class) {
                return new CalendarController(1L);
            }
            try { return type.getDeclaredConstructor().newInstance(); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        Scene scene = new Scene(loader.load(), 1000, 750);
        stage.setTitle("Calendar Test");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
