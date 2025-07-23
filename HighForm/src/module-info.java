module HighForm {

	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.base;
	requires javafx.graphics;
	requires lombok;
	requires java.sql;

    

    /* ───── 런처에서 접근해야 하는 패키지 ───── */
    exports com.mypage;                        

    /* JavaFX 런처가 리플렉션으로 new 할 때 접근 허용 */
    opens com.mypage to javafx.graphics;      
    
    
    opens com to javafx.graphics, javafx.fxml;
    opens com.manager.controller to javafx.fxml; 
    opens com.login.controller to javafx.fxml;
    opens com.board to javafx.graphics, javafx.fxml;  // 이 줄 추가
    opens com.board.controller to javafx.fxml;
    opens com.mypage.controller to javafx.fxml;
}
