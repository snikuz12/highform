package com.login.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SplashController {
    @FXML
    private Rectangle cursor;

    @FXML
    public void initialize() {
        FadeTransition blink = new FadeTransition(Duration.millis(500), cursor);
        blink.setFromValue(1.0);
        blink.setToValue(0.0);
        blink.setCycleCount(FadeTransition.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();
    }
}