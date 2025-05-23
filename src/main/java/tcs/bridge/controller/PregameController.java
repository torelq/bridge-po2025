package tcs.bridge.controller;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.view.BiddingView;
import tcs.bridge.view.PregameView;

public class PregameController{
    private final Stage stage;
    private final Game game;
    public PregameController(Stage stage, Game game) {
        this.stage = stage;
        this.game = game;
    }

    public void onClickJoin(ActionEvent event){
        /* MAKING SIMPLE GAME */
        game.joinGame(new Player(Player.Position.NORTH));
        game.joinGame(new Player(Player.Position.EAST));
        game.joinGame(new Player(Player.Position.SOUTH));
        game.joinGame(new Player(Player.Position.WEST));

        /* START BIDDING */
        startBidding();
    }

    private void startBidding(){
        BiddingController controller = new BiddingController(stage, game);
        BiddingView view = new BiddingView(game, controller);

        stage.setTitle("TCS Bridge - BIDDING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
