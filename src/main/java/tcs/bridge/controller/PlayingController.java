package tcs.bridge.controller;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.model.Card;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.view.FinishedView;

import java.util.ArrayList;
import java.util.List;
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

    public PlayingController(Label leftLabel, Label rightLabel) {
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
    }

    /* PLYING CARDS AND CHECKING IF FINISHED */
    public void onCardClicked(MouseEvent event, Card card, ImageView imageView, int position) {
        // imageView = (ImageView)event.getSource();
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
            if (game.getState() == Game.State.FINISHED){startFinished();}
            makeTurn(game.getCurrentTurn());
        }
    }

    /* MAKING BIGGER LABEL FOR ACTUAL PLAYING PLAYER */
    public void makeTurn(Player.Position position) {
        for (int i = 0; i < labels.size(); i++) {
            int gainedTricks;
            if (game.getPlayers().get(Player.Position.values()[i]).getPosition() == game.getContract().getDeclarer()
                    || game.getPlayers().get(Player.Position.values()[i]).getPosition() == game.getContract().getDummy())
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

    public void startFinished(){
        FinishedController controller = new FinishedController();
        FinishedView view = new FinishedView(controller);

        stage.setTitle("TCS Bridge - FINISHED");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
