package tcs.bridge.view;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import tcs.bridge.controller.Controller;
import tcs.bridge.model.Player;

import java.util.AbstractMap;
import static tcs.bridge.App.game;

public class FinishedView extends StackPane {
    public FinishedView(Controller controller) {
        this.setStyle("-fx-background-color: #32442d;");

        Label deckResult = new Label();
        AbstractMap.SimpleEntry<Player.Position, Player.Position> winner = game.getWinner();
        deckResult.setText("Winner: \n" + winner.getKey() + " - " + winner.getValue());
        deckResult.setTextFill(Color.BEIGE);
        deckResult.setTextAlignment(TextAlignment.CENTER);
        deckResult.setStyle("-fx-font-size: 100");
        this.getChildren().add(deckResult);
    }
}
