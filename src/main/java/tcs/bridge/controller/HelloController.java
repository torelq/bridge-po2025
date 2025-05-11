package tcs.bridge.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

// TODO should be replaced
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}