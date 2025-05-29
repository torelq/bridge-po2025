package tcs.bridge.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.App;
import tcs.bridge.communication.messages.*;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.model.*;
import tcs.bridge.view.PlayingView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static tcs.bridge.App.clientMessageStream;
import static tcs.bridge.App.server;
import static tcs.bridge.App.stage;
import static tcs.bridge.App.game;
import static tcs.bridge.App.myPosition;
import static tcs.bridge.App.playerNames;


public class BiddingController {

    public List<Label> labels;
    public StackPane table;
    public GridPane biddingGrid;
    public Label inforamtionLeftLabel;
    public Label inforamtionRightLabel;

    public BiddingController() {
        Thread readerTh = new Thread(() -> readerThread(App.clientMessageStream));
        readerTh.start();

        labels = new ArrayList<>();
        for (Player.Position pos : Player.Position.values()) {
            labels.add(new Label(pos.name() + "\n" + playerNames.get(pos.ordinal()) + (pos == myPosition ? " (me)" : "")));
        }
        table = new StackPane();
    }

    private void readerThread(ClientMessageStream clientMessageStream) {
        try {
            while (true) {
                ServerToClientMessage message = clientMessageStream.readMessage();
                System.out.println(message);
                if (message instanceof JoinGameNotice joinGameNotice){
                    playerNames.set(joinGameNotice.position().ordinal(), joinGameNotice.name());
                    Platform.runLater(() -> {
                        labels.get(joinGameNotice.position().ordinal()).setText(joinGameNotice.position().name()
                                + "\n" + playerNames.get(joinGameNotice.position().ordinal())
                                + (joinGameNotice.position() == myPosition ? " (me)" : ""));
                    });
                }
                if (message instanceof MakeBidNotice) {
                    Platform.runLater(()->{
                        Bidding.Bid bid = ((MakeBidNotice) message).bid();
                        game.makeBid(bid);
                        if (!bid.isSpecial())
                            inforamtionLeftLabel.setText(bid.toString());
                        makeTurn(game.getCurrentTurn());
                        if (game.getState() == Game.State.PLAYING){
                            startPlaying();
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* BIDDING AND CHECKING IF END OF BIDDING */
    public void onBidButtonClicked(ActionEvent event, Bidding.Bid bid) {
        if (game.getBidding().canBid(game.getCurrentTurn(), bid)){
            try {
                App.clientMessageStream.writeMessage(new MakeBidRequest(game.getCurrentTurn(), bid));
            } catch (IOException e) {
                throw new RuntimeException(e);
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
        PlayingController controller = new PlayingController(inforamtionLeftLabel, inforamtionRightLabel);
        PlayingView view = new PlayingView(controller);

        stage.setTitle("TCS Bridge - PLAYING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
