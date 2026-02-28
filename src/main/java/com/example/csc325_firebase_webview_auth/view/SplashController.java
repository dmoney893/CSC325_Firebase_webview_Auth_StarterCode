package com.example.csc325_firebase_webview_auth.view;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.util.Duration;

public class SplashController {

    @FXML
    public void initialize() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            try {
                App.setRoot("/files/AccessFBView.fxml");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        pause.play();
    }
}
