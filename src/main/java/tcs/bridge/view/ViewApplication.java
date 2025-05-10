package tcs.bridge.view;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.model.*;


import java.util.List;

public class ViewApplication extends Application {
    static Game game = new Game();
    static Player playerNorth = new Player(Player.Position.NORTH);
    static Player playerEast = new Player(Player.Position.EAST);
    static Player playerSouth = new Player(Player.Position.SOUTH);
    static Player playerWest = new Player(Player.Position.WEST);


    public Scene makeTable(List<Hand> deal, Stage stage){
        // Setting a scene
        BorderPane deck = new BorderPane();
        StackPane table = new StackPane();
        table.setAlignment(Pos.CENTER);
        // adding all cards to deck
        for (int i = 0; i < deal.size(); i++) {
            StackPane player = new StackPane();
            player.setAlignment(Pos.CENTER);
            int translate = -283;
            for (Card card : deal.get(i).getCards()) {
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
                // if player clicked good card, play it and move it on the table
                imageView.setOnMouseClicked(event -> {
                    if (game.playCard(card)){
                        player.getChildren().remove(imageView);
                        imageView.setTranslateX(0); imageView.setTranslateY(0);
                        switch (finalI) {
                            case 0: imageView.setTranslateY(-50); break;
                            case 1: imageView.setTranslateX(50); break;
                            case 2: imageView.setTranslateY(50); break;
                            case 3: imageView.setTranslateX(-50); break;
                        }
                        table.getChildren().add(imageView);
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

        deck.setCenter(table);
        deck.setStyle("-fx-background-color: #32442d;");

        //Creating a scene object
        return new Scene(deck, 900, 900);
    }


    /* MAIN LOOP OF THE GAME */
    @Override
    public void start(Stage stage) throws Exception {
        //Instantiating the BorderPane class
        Deck deck = new Deck();
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
