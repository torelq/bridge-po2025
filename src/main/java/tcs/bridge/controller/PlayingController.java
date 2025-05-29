package tcs.bridge.controller;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.App;
import tcs.bridge.communication.messages.*;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.model.Bidding;
import tcs.bridge.model.Card;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.view.FinishedView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static tcs.bridge.App.clientMessageStream;
import static tcs.bridge.App.server;
import static tcs.bridge.App.stage;
import static tcs.bridge.App.game;
import static tcs.bridge.App.myPosition;
import static tcs.bridge.App.playerNames;

public class PlayingController {

    public List<Label> labels;
    public StackPane table;
    public List<StackPane> playersPanes;
    public Label inforamtionLeftLabel;
    public Label inforamtionRightLabel;
    public Map<Card, ImageView> cardImages;

    public PlayingController(Label leftLabel, Label rightLabel) {
        Thread readerTh = new Thread(() -> readerThread());
        readerTh.start();
        inforamtionLeftLabel = leftLabel;
        inforamtionRightLabel = rightLabel;

        labels = new ArrayList<>();
        for (Player.Position pos : Player.Position.values()) {
            labels.add(new Label(pos.name() + "\n" + playerNames.get(pos.ordinal()) + (pos == myPosition ? " (me)" : "")));
        }
        table = new StackPane();
        playersPanes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            playersPanes.add(new StackPane());
        }
        cardImages = new HashMap<>();
    }

    private void readerThread() {
        try {
            AtomicBoolean changeState = new AtomicBoolean(false);
            while (!changeState.get()) {
                System.out.println("Waiting for Playing message...");
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
                if (message instanceof PlayCardNotice) {
                    Card card = ((PlayCardNotice) message).card();
                    int position = ((PlayCardNotice) message).position().ordinal();
                    Platform.runLater(()->{
                        ImageView imageView = cardImages.get(card);
                        if (game.playCard(card)){
                            if (game.getNumberOfPlayedCards() % 4 == 1)
                                table.getChildren().clear();
                            playersPanes.get(position).getChildren().remove(imageView);
                            imageView.setTranslateX(0); imageView.setTranslateY(0);
                            switch (position) {
                                case 0: imageView.setTranslateY(-50); break;
                                case 1: imageView.setTranslateX(50); break;
                                case 2: imageView.setTranslateY(50); break;
                                case 3: imageView.setTranslateX(-50); break;
                            }
                            table.getChildren().add(imageView);
                            if (game.getState() == Game.State.FINISHED){
                                changeState.set(true);
                                startFinished();
                            }
                            makeTurn(game.getCurrentTurn());
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* PLYING CARDS AND CHECKING IF FINISHED */
    public void onCardClicked(MouseEvent event, Card card, int position) {
        try {
            Player.Position pos = Player.Position.values()[position];
            clientMessageStream.writeMessage(new PlayCardRequest(pos, card));
        } catch (IOException e) {
            System.out.println("IO Exception in PlayingController.onCardClicked");
            throw new RuntimeException(e);
        }
    }

    /* MAKING BIGGER LABEL FOR ACTUAL PLAYING PLAYER */
    public void makeTurn(Player.Position position) {
        for (int i = 0; i < labels.size(); i++) {
            Player.Position pos = Player.Position.values()[i];
            int gainedTricks;
            if (game.getPlayers().get(pos).getPosition() == game.getContract().getDeclarer()
                    || game.getPlayers().get(pos).getPosition() == game.getContract().getDummy())
                gainedTricks = game.getGainedTricks();
            else
                gainedTricks = game.getCompleteTricks().size() - game.getGainedTricks();
            labels.get(i).setText(pos.toString() + " " + gainedTricks + "\n" +
                    playerNames.get(i) + (pos == myPosition ? " (me)" : "")
            );
            if (Player.Position.values()[i].toString().equals(position.toString()))
                labels.get(i).setStyle("-fx-font-size: 20; -fx-font-weight: bold");
            else
                labels.get(i).setStyle("-fx-font-weight: normal; -fx-font-size: 15");
        }
    }

    public void startFinished(){
        FinishedController controller = new FinishedController();
        FinishedView view = new FinishedView(controller);

        stage.setTitle("TCS Bridge - FINISHED");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
