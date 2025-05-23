package tcs.bridge.controller;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.model.*;
import tcs.bridge.view.PlayingView;

import java.util.ArrayList;
import java.util.List;

public class BiddingController {
    private final Stage stage;
    private final Game game;

    public List<Label> labels;
    public StackPane table;
    public GridPane biddingGrid;
    public Label inforamtionLeftLabel;
    public Label inforamtionRightLabel;

    public BiddingController(Stage stage, Game game) {
        this.stage = stage;
        this.game = game;

        Label labelNorth = new Label("NORTH");
        Label labelEast = new Label("EAST");
        Label labelSouth = new Label("SOUTH");
        Label labelWest = new Label("WEST");
        labels = new ArrayList<>(List.of(labelNorth, labelEast, labelSouth, labelWest));
        table = new StackPane();
    }

    /* BIDDING AND CHECKING IF END OF BIDDING */
    public void onBidButtonClicked(ActionEvent event, Bidding.Bid bid) {
        if (game.getBidding().canBid(game.getCurrentTurn(), bid)) {
            game.makeBid(bid);
            if (!bid.isSpecial())
                inforamtionLeftLabel.setText(bid.toString());
            makeTurn(game.getCurrentTurn());
            if (game.getState() == Game.State.PLAYING){
                startPlaying();
            }
        }
    }

    /* MAKING BIGGER LABEL FOR ACTUAL BIDING PLAYER */
    public void makeTurn(Player.Position position) {
        for (int i = 0; i < labels.size(); i++) {
            if (Player.Position.values()[i].toString().equals(position.toString()))
                labels.get(i).setStyle("-fx-font-size: 30");
            else
                labels.get(i).setStyle("-fx-font-size: 15");
        }
    }

    private void startPlaying(){
        PlayingController controller = new PlayingController(stage, game, inforamtionLeftLabel, inforamtionRightLabel);
        PlayingView view = new PlayingView(game, controller);

        stage.setTitle("TCS Bridge - PLAYING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
