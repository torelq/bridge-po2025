package tcs.bridge.controller;

import javafx.stage.Stage;
import tcs.bridge.model.Game;

public class FinishedController {
    private final Stage stage;
    private final Game game;

    public FinishedController(Stage stage, Game game) {
        this.stage = stage;
        this.game = game;
    }

}
