package tcs.bridge.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import tcs.bridge.App;
import tcs.bridge.controller.PlayingController;
import tcs.bridge.model.Card;
import tcs.bridge.model.Game;
import tcs.bridge.model.Hand;
import tcs.bridge.model.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayingView extends BorderPane {
    private final Game game;
    private final PlayingController controller;

    public PlayingView(Game game, PlayingController controller) {
        this.game = game;
        this.controller = controller;
        this.setStyle("-fx-background-color: #32442d;");

        /* SETTING A SCENE FOR A PLAY */
        List<Hand> deal = game.getDeck().deal();
        BorderPane center = new BorderPane();
        controller.table.setAlignment(Pos.CENTER);
        // adding all cards to deck
        for (int i = 0; i < deal.size(); i++) {
            StackPane player = new StackPane();
            player.setAlignment(Pos.CENTER);
            int translate = -283;
            List<Card> cards = deal.get(i).getCards();
            for (Card card : cards) {
                ImageView imageView = new ImageView(String.valueOf(App.class.
                        getResource("/tcs/bridge/view/cards/" +
                                card.getSuit().getName().toLowerCase() + "_" + card.getRank().getName() + ".png")));
                imageView.setFitWidth(96.8);
                imageView.setFitHeight(136);
                if (i == 0 || i == 2)
                    imageView.setTranslateX(translate += 40);
                else
                    imageView.setTranslateY(translate += 40);
                int finalI = i;
                imageView.setOnMouseClicked(mouseEvent ->
                        controller.onCardClicked(mouseEvent, card, imageView, finalI));
                player.getChildren().add(imageView);
            }
            switch (i){
                case 0: this.setTop(player); break;
                case 1: this.setRight(player); break;
                case 2: this.setBottom(player); break;
                case 3: this.setLeft(player); break;
            }
        }
        /* LABEL FOR PLAYERS, BACKGROUND */
        center.setCenter(controller.table);

        controller.labelNorth.setTextAlignment(TextAlignment.CENTER);
        controller.labelNorth.setTextFill(Color.BEIGE);
        controller.labelNorth.setStyle("-fx-font-size: 15;");
        center.setTop(new StackPane(controller.labelNorth));

        controller.labelEast.setTextAlignment(TextAlignment.CENTER);
        controller.labelEast.setTextFill(Color.BEIGE);
        controller.labelEast.setStyle("-fx-font-size: 15;");
        center.setRight(new StackPane(controller.labelEast));

        controller.labelWest.setTextAlignment(TextAlignment.CENTER);
        controller.labelWest.setTextFill(Color.BEIGE);
        controller.labelWest.setStyle("-fx-font-size: 15;");
        center.setLeft(new StackPane(controller.labelWest));

        controller.labelSouth.setTextAlignment(TextAlignment.CENTER);
        controller.labelSouth.setTextFill(Color.BEIGE);
        controller.labelSouth.setStyle("-fx-font-size: 15;");
        center.setBottom(new StackPane(controller.labelSouth));
        controller.makeTurn(game.getCurrentTurn());

        this.setCenter(center);
    }

}
