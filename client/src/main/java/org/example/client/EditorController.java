package org.example.client;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class EditorController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}