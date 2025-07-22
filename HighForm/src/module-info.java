module HighForm {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires javafx.graphics;
	requires lombok;
	requires java.sql;
	
    opens com to javafx.graphics, javafx.fxml;
    opens com.login.controller to javafx.fxml;
    opens com.board to javafx.graphics, javafx.fxml;  // 이 줄 추가
    opens com.board.controller to javafx.fxml;
}