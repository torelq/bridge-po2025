package tcs.bridge.view;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import tcs.bridge.model.Bidding;
import tcs.bridge.model.Card;
import tcs.bridge.model.Game;
import tcs.bridge.model.Hand;
import tcs.bridge.model.Player;
import tcs.bridge.model.Suit;

public class ViewApplication extends Application {
    static Game game = new Game();
    static Player playerNorth = new Player(Player.Position.NORTH);
    static Player playerEast = new Player(Player.Position.EAST);
    static Player playerSouth = new Player(Player.Position.SOUTH);
    static Player playerWest = new Player(Player.Position.WEST);
    List<Player> players = new ArrayList<>(List.of(playerNorth, playerEast, playerSouth, playerWest));
    static Label labelNorth = new Label("NORTH");
    static Label labelEast = new Label("EAST");
    static Label labelSouth = new Label("SOUTH");
    static Label labelWest = new Label("WEST");
    List<Label> labels = new ArrayList<>(List.of(labelNorth, labelEast, labelSouth, labelWest));
    static StackPane table = new StackPane();

    public void showResult(Stage stage) {
        table.getChildren().clear();
        Label deckResult = new Label();
        AbstractMap.SimpleEntry<Player.Position, Player.Position> winner = game.getWinner();
        deckResult.setText("Winner: \n" + winner.getKey() + " - " + winner.getValue());
        deckResult.setTextFill(Color.BEIGE);
        deckResult.setTextAlignment(TextAlignment.CENTER);
        deckResult.setStyle("-fx-font-size: 100");
        table.getChildren().add(deckResult);
    }

    /* MAKING BIGGER LABEL FOR ACTUAL PLAYING PLAYER */
    public void makeTurn(Player.Position position) {
        for (int i = 0; i < labels.size(); i++) {
            int gainedTricks;
            if (players.get(i).getPosition() == game.getContract().getDeclarer() || players.get(i).getPosition() == game.getContract().getDummy())
                gainedTricks = game.getGainedTricks();
            else
                gainedTricks = game.getCompleteTricks().size() - game.getGainedTricks();
            labels.get(i).setText(Player.Position.values()[i].toString() + " " + gainedTricks);
            if (Player.Position.values()[i].toString().equals(position.toString()))
                labels.get(i).setStyle("-fx-font-size: 30");
            else
                labels.get(i).setStyle("-fx-font-size: 15");
        }
    }

    /* SETTING A SCENE FOR A PLAY */
    public Scene makeTable(List<Hand> deal, Stage stage){
        BorderPane deck = new BorderPane();
        BorderPane center = new BorderPane();
        table.setAlignment(Pos.CENTER);
        // adding all cards to deck
        for (int i = 0; i < deal.size(); i++) {
            StackPane player = new StackPane();
            player.setAlignment(Pos.CENTER);
            int translate = -283;
            List<Card> cards = deal.get(i).getCards();
            for (Card card : cards) {
                ImageView imageView = new ImageView(String.valueOf(ViewApplication.class.
                        getResource("/tcs/bridge/view/cards/" +
                                card.getSuit().getName().toLowerCase() + "_" + card.getRank().getName() + ".png")));
                imageView.setFitWidth(96.8);
                imageView.setFitHeight(136);
                if (i == 0 || i == 2)
                    imageView.setTranslateX(translate += 40);
                else
                    imageView.setTranslateY(translate += 40);
                int finalI = i;
                /* PLYING CARDS AND CHECKING IF FINISHED */
                imageView.setOnMouseClicked(event -> {
                    if (game.playCard(card)){
                        if (game.getNumberOfPlayedCards() % 4 == 1)
                            table.getChildren().clear();
                        player.getChildren().remove(imageView);
                        imageView.setTranslateX(0); imageView.setTranslateY(0);
                        switch (finalI) {
                            case 0: imageView.setTranslateY(-50); break;
                            case 1: imageView.setTranslateX(50); break;
                            case 2: imageView.setTranslateY(50); break;
                            case 3: imageView.setTranslateX(-50); break;
                        }
                        table.getChildren().add(imageView);
                        if (game.getState() == Game.State.FINISHED){showResult(stage);}
                        makeTurn(game.getCurrentTurn());
                    }

                });
                player.getChildren().add(imageView);
            }
            switch (i){
                case 0: deck.setTop(player); break;
                case 1: deck.setRight(player); break;
                case 2: deck.setBottom(player); break;
                case 3: deck.setLeft(player); break;
            }
        }
        /* LABEL FOR PLAYERS, BACKGROUND */
        center.setCenter(table);

        labelNorth.setTextAlignment(TextAlignment.CENTER);
        labelNorth.setTextFill(Color.BEIGE);
        labelNorth.setStyle("-fx-font-size: 15;");
        center.setTop(new StackPane(labelNorth));

        labelEast.setTextAlignment(TextAlignment.CENTER);
        labelEast.setTextFill(Color.BEIGE);
        labelEast.setStyle("-fx-font-size: 15;");
        center.setRight(new StackPane(labelEast));

        labelWest.setTextAlignment(TextAlignment.CENTER);
        labelWest.setTextFill(Color.BEIGE);
        labelWest.setStyle("-fx-font-size: 15;");
        center.setLeft(new StackPane(labelWest));

        labelSouth.setTextAlignment(TextAlignment.CENTER);
        labelSouth.setTextFill(Color.BEIGE);
        labelSouth.setStyle("-fx-font-size: 15;");
        center.setBottom(new StackPane(labelSouth));
        makeTurn(game.getCurrentTurn());

        deck.setCenter(center);
        deck.setStyle("-fx-background-color: #32442d;");

        //Creating a scene object
        return new Scene(deck, 900, 900);
    }


    /* MAIN LOOP OF THE GAME */
    @Override
    public void start(Stage stage) {
        //Instantiating the BorderPane class
        Scene scene = makeTable(game.getDeck().deal(), stage);
        stage.setTitle("Deck View");
        stage.setScene(scene);
        stage.show();
    }

    // testowe
    public static void main(String[] args) {
        /* MAKING SIMPLE GAME */
        game.joinGame(playerNorth);
        game.joinGame(playerEast);
        game.joinGame(playerSouth);
        game.joinGame(playerWest);

        Bidding.Bid bid = new Bidding.Bid(1, Suit.HEARTS);
        Bidding.Bid passBid = Bidding.Bid.PASS_BID;
        game.makeBid(bid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        launch();
    }
}
