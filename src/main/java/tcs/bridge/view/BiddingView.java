package tcs.bridge.view;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import tcs.bridge.controller.BiddingController;
import tcs.bridge.controller.PregameController;
import tcs.bridge.model.Bidding;
import tcs.bridge.model.Game;

public class BiddingView extends BorderPane {
    private final Game game;
    private final BiddingController controller;

    public BiddingView(Game game, BiddingController controller){
        this.game = game;
        this.controller = controller;
        this.setStyle("-fx-background-color: #32442d;");

        Button joinButton = new Button("BID 1 of Hearts");
        joinButton.setOnAction(controller::onClickBid);
        this.setCenter(joinButton);
    }
}
