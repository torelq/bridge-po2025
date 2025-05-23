package tcs.bridge.controller;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tcs.bridge.model.Bidding;
import tcs.bridge.model.Game;
import tcs.bridge.model.Suit;
import tcs.bridge.view.PlayingView;

public class BiddingController {
    private final Stage stage;
    private final Game game;
    public BiddingController(Stage stage, Game game) {
        this.stage = stage;
        this.game = game;
    }
    public void onClickBid(ActionEvent event){
        /* MAKING SIMPLE BID */
        Bidding.Bid bid = new Bidding.Bid(1, Suit.HEARTS);
        Bidding.Bid passBid = Bidding.Bid.PASS_BID;
        game.makeBid(bid);
        game.makeBid(passBid);
        game.makeBid(passBid);
        game.makeBid(passBid);

        /* START PLAYING */
        startPlaying();
    }

    private void startPlaying(){
        PlayingController controller = new PlayingController(stage, game);
        PlayingView view = new PlayingView(game, controller);

        stage.setTitle("TCS Bridge - PLAYING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
