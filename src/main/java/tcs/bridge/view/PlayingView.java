package tcs.bridge.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import tcs.bridge.App;
import tcs.bridge.controller.Controller;
import tcs.bridge.model.Card;
import tcs.bridge.model.Hand;

import java.util.List;
import static tcs.bridge.App.game;
import static tcs.bridge.App.debugMode;

public class PlayingView extends BorderPane {

    public PlayingView(Controller controller) {
        this.setStyle("-fx-background-color: #32442d;");


        /* SETTING A SCENE FOR A PLAY */
        List<Hand> deal = game.getDeck().deal();
        BorderPane center = new BorderPane();
        controller.table.setAlignment(Pos.CENTER);
        Button scoreBoard = new Button("Scoreboard");
        scoreBoard.setOnAction(controller::onClickScoreboard);
        VBox rightvbox = new VBox(10, scoreBoard, controller.inforamtionRightLabel);

        /* ADDING ALL CARDS TO DECK */
        for (int i = 0; i < deal.size(); i++) {
            StackPane player = controller.playersPanes.get(i);
            player.setAlignment(Pos.CENTER);
            int translate = -283;
            List<Card> cards = deal.get(i).getCards();
            for (Card card : cards) {
                ImageView imageView;
                if (debugMode || i == App.myPosition.ordinal() || i == game.getContract().getDummy().ordinal()) {
                    imageView = new ImageView(String.valueOf(App.class.
                            getResource("/tcs/bridge/view/cards/" +
                                    card.getSuit().getName().toLowerCase() + "_" + card.getRank().getName() + ".png")));
                } else { //(i != App.myPosition.ordinal())
                    imageView = new ImageView(String.valueOf(App.class.
                            getResource("/tcs/bridge/view/cards/back_bic.png")));
                }
                imageView.setFitWidth(96.8);
                imageView.setFitHeight(136);
                if (i == 0 || i == 2)
                    imageView.setTranslateX(translate += 40);
                else
                    imageView.setTranslateY(translate += 40);
                int finalI = i;
                controller.cardImages.put(card, imageView);
                imageView.setOnMouseClicked(mouseEvent ->
                        controller.onCardClicked(mouseEvent, card, finalI));
                player.getChildren().add(imageView);
            }
            switch (i){
                case 0:
                    player.setMinSize(600, 0);
                    this.setTop(new HBox(20,
                            new StackPane(controller.inforamtionLeftLabel),
                            player,
                            rightvbox));
                    this.getTop().setStyle("-fx-alignment: center;");
                    break;
                case 1: this.setRight(player); break;
                case 2: this.setBottom(player); break;
                case 3: this.setLeft(player); break;
            }
        }
        /* LABEL FOR PLAYERS */
        center.setCenter(controller.table);

        for (int i = 0; i < 4; ++i){
            controller.labels.get(i).setTextAlignment(TextAlignment.CENTER);
            controller.labels.get(i).setTextFill(Color.BEIGE);
            controller.labels.get(i).setStyle("-fx-font-size: 15;");
            StackPane label = new StackPane(controller.labels.get(i));
            label.setAlignment(Pos.CENTER);
            switch (i){
                case 0: center.setTop(label); break;
                case 1: center.setRight(label); break;
                case 2: center.setBottom(label); break;
                case 3: center.setLeft(label); break;
            }
        }

        controller.makeTurn(game.getCurrentTurn());

        this.setCenter(center);
    }

}
