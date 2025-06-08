package tcs.bridge.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import tcs.bridge.controller.Controller;
import tcs.bridge.model.Player;

import java.util.AbstractMap;
import static tcs.bridge.App.game;
import static tcs.bridge.App.scoringEntryList;

public class FinishedView extends BorderPane {
    public FinishedView(Controller controller, ScoreboardView scoreboardView) {
        this.setStyle("-fx-background-color: #32442d;");

        Button playNextRoundButton = new Button("Play Next Round");
        playNextRoundButton.setOnAction(controller::onPlayNextRound);
        StackPane stackPane = new StackPane(playNextRoundButton);
        stackPane.setMinSize(150, 150);
        this.setTop(stackPane);

        scoreboardView.setMaxSize(400, 400);
        this.setCenter(scoreboardView);
    }
}
