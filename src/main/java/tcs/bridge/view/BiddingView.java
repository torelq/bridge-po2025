package tcs.bridge.view;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import tcs.bridge.App;
import static tcs.bridge.App.game;
import static tcs.bridge.App.debugMode;
import tcs.bridge.controller.Controller;
import tcs.bridge.model.Bidding;
import tcs.bridge.model.Card;
import tcs.bridge.model.Hand;
import tcs.bridge.model.Suit;

public class BiddingView extends BorderPane {
    public BiddingView(Controller controller){
        this.setStyle("-fx-background-color: #32442d;");

        /* SETTING A SCENE FOR A BIDDING */
        BorderPane center = new BorderPane();
        controller.table.setAlignment(Pos.CENTER);

        /* SETTING GRID FOR BIDDING */
        controller.biddingGrid = new GridPane();
        controller.biddingGrid.setHgap(10);
        controller.biddingGrid.setVgap(10);

        int row = 0;
        List<Suit> suits = new ArrayList<>(List.of(Suit.values()[1], Suit.values()[0], Suit.values()[2], Suit.values()[3]));
        suits.add(Suit.NO_TRUMP);
        for (int level = 1; level <= 7; level++) {
            int col = 0;
            for (Suit suit : suits) {
                Bidding.Bid bid = new Bidding.Bid(level, suit);
                Button button = new Button(bid.toString());
                button.setOnAction(e -> controller.onBidButtonClicked(e, bid));
                controller.biddingGrid.add(button, col, row);
                col++;
            }
            row++;
        }

        int col = 0;
        for (Bidding.Bid.SpecialBid specialBid : Bidding.Bid.SpecialBid.values()) {
            Bidding.Bid bid = new Bidding.Bid(0, null, specialBid);
            Button button = new Button(bid.toString());
            button.setOnAction(e -> controller.onBidButtonClicked(e, bid));
            controller.biddingGrid.add(button, col++, row);
        }
        controller.updateBiddingGridColors();
        controller.biddingGrid.setAlignment(Pos.CENTER);
        controller.table.getChildren().add(controller.biddingGrid);

        controller.inforamtionLeftLabel = new Label();
        controller.inforamtionRightLabel = new Label();
        controller.inforamtionLeftLabel.setTextAlignment(TextAlignment.CENTER);
        controller.inforamtionLeftLabel.setTextFill(Color.BEIGE);
        controller.inforamtionLeftLabel.setStyle("-fx-font-size: 30;");

        controller.inforamtionRightLabel.setTextAlignment(TextAlignment.CENTER);
        controller.inforamtionRightLabel.setTextFill(Color.BEIGE);
        controller.inforamtionRightLabel.setStyle("-fx-font-size: 20;");

        Button scoreBoard = new Button("Scoreboard");
        scoreBoard.setOnAction(controller::onClickScoreboard);
        VBox rightvbox = new VBox(10, scoreBoard, controller.inforamtionRightLabel);
        rightvbox.setAlignment(Pos.CENTER);

        if (App.server != null) {
            try {
                controller.inforamtionRightLabel.setText("Port:\n" + App.server.getPort());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /* ADDING ALL CARDS TO DECK */
        List<Hand> deal = game.getDeck().deal();
        for (int i = 0; i < deal.size(); i++) {
            StackPane player = new StackPane(); // TODO: nie tak
            player.setAlignment(Pos.CENTER);
            int translate = -283;
            List<Card> cards = deal.get(i).getCards();
            for (Card card : cards) {
                ImageView imageView;
                if (debugMode || i == App.myPosition.ordinal()) {
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
//            controller.labels.get(i).setMinSize(150, 40);
//            controller.labels.get(i).setMaxSize(150, 40);
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

