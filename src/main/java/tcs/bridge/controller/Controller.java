package tcs.bridge.controller;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.App;
import tcs.bridge.communication.messages.*;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;
import tcs.bridge.model.*;
import tcs.bridge.server.Server;
import tcs.bridge.view.BiddingView;
import tcs.bridge.view.FinishedView;
import tcs.bridge.view.PlayingView;
import tcs.bridge.view.ScoreboardView;

import static tcs.bridge.App.*;


public class Controller {
    /* BIDDING AND PLAYING */
    public List<Label> labels;
    public StackPane table;
    public GridPane biddingGrid;
    public Label inforamtionLeftLabel;
    public Label inforamtionRightLabel;
    public List<StackPane> playersPanes;
    public Map<Card, ImageView> cardImages;

    /* PREGAME */
    public TextField portField;
    public TextField playerNameField;
    public TextField hostField;
    public Label lblPort;
    public Label lblHost;
    public HBox pregameBox;
    public ComboBox<Player.Position> positionComboBox;
    public ToggleButton debugModeToggle;

    /* INIT CONTROLLER */
    public Controller() {
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
        scoringEntryList = new ArrayList<>();
    }
    /* READING MESSAGE THREAD */
    private void readerThread() {
        try {
            while (true) {
                ServerToClientMessage message = clientMessageStream.readMessage();
                System.out.println(message);

                /* GETTING GAME STATE FROM SERVER */
                if (message instanceof StateRequest.StateResponse stateResponse) {
                    Platform.runLater(() -> {
                        myPosition = stateResponse.myPosition();
                        if (myPosition != null) {
                            for (Player.Position pos : Player.Position.values()) {
                                labels.set(pos.ordinal(), new Label(pos.name() + "\n" + playerNames.get(pos.ordinal()) + (pos == myPosition ? " (me)" : "")));
                            }
                        }

                        game = stateResponse.game(); // proper deep copy

//                        Game serverGame = stateResponse.game();
//                        List<Hand> hands = serverGame.getDeck().deal();
//                        game = new Game();
//                        game.joinGame(serverGame.getPlayers().get(Player.Position.NORTH));
//                        game.joinGame(serverGame.getPlayers().get(Player.Position.EAST));
//                        game.joinGame(serverGame.getPlayers().get(Player.Position.SOUTH));
//                        game.joinGame(serverGame.getPlayers().get(Player.Position.WEST));
//                        game.setDeck(serverGame.getDeck());
//                        game.getPlayers().get(Player.Position.NORTH).setHand(hands.get(0));
//                        game.getPlayers().get(Player.Position.EAST).setHand(hands.get(1));
//                        game.getPlayers().get(Player.Position.SOUTH).setHand(hands.get(2));
//                        game.getPlayers().get(Player.Position.WEST).setHand(hands.get(3));
                        switch (game.getState()){
                            case BIDDING -> startBidding();
                            case PLAYING -> startPlaying();
                            case FINISHED -> startFinished();
                        }
                    });
                }
                /* JOIN GAME AND SETTING MY NAME AND POSITION */
                if (message instanceof JoinGameNotice joinGameNotice){
                    Platform.runLater(() -> {
                        playerNames.set(joinGameNotice.position().ordinal(), joinGameNotice.name());
                        labels.get(joinGameNotice.position().ordinal()).setText(joinGameNotice.position().name()
                                + "\n" + playerNames.get(joinGameNotice.position().ordinal())
                                + (joinGameNotice.position() == myPosition ? " (me)" : ""));
                    });
                }
                /* MAKING BID */
                if (message instanceof MakeBidNotice makeBidNotice) {
                    Platform.runLater(()->{
                        Bidding.Bid bid = makeBidNotice.bid();
                        game.makeBid(bid);
                        inforamtionLeftLabel.setText(game.getBidding().lastBidToString());

                        makeTurn(game.getCurrentTurn());
                        updateBiddingGridColors();
                        if (game.getState() == Game.State.PLAYING){
                            startPlaying();
                        }
                    });
                }
                /* PLAYING CARD - MOVING TO THE CENTER */
                if (message instanceof PlayCardNotice playCardNotice) {
                    Platform.runLater(()->{
                        Card card = playCardNotice.card();
                        int position = playCardNotice.position().ordinal();
                        ImageView imageView = cardImages.get(card);
                        if (game.playCard(card)){
                            if (game.getNumberOfPlayedCards() % 4 == 1)
                                table.getChildren().clear();
                            playersPanes.get(position).getChildren().remove(imageView);
                            imageView.setImage(new Image(String.valueOf(App.class.
                                    getResource("/tcs/bridge/view/cards/" +
                                            card.getSuit().getName().toLowerCase() +
                                            "_" + card.getRank().getName() + ".png"))));
                            imageView.setTranslateX(0); imageView.setTranslateY(0);
                            switch (position) {
                                case 0: imageView.setTranslateY(-50); break;
                                case 1: imageView.setTranslateX(50); break;
                                case 2: imageView.setTranslateY(50); break;
                                case 3: imageView.setTranslateX(-50); break;
                            }
                            table.getChildren().add(imageView);
                            if (game.getState() == Game.State.PLAYING)
                                makeTurnPlaying(game.getCurrentTurn());
                        }
                        else {
                            System.out.println("Playing card FAILED.");
                        }
                        if (debugMode && game.getState() == Game.State.PLAYING)
                            onDebugButtonClicked(null);
                    });
                }

                /* FINISH THIS */
                if (message instanceof GameFinishedNotice gameFinishedNotice) {
                    Platform.runLater(()->{
                        scoringEntryList.add(gameFinishedNotice.scoringEntry());
                        startFinished();
//                        try {
//                            clientMessageStream.writeMessage(new ScoringRequest());
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
                    });
                }

                if (message instanceof ScoringRequest.ScoringResponse scoringResponse) {
                    Platform.runLater(()-> {
                        scoringEntryList = scoringResponse.scoring().getScoring();

                    });
                }

                /* PLAY AGAIN */
                if (message instanceof NewGameNotice) {
                    Platform.runLater(()->{
                        try {
                            clientMessageStream.writeMessage(new StateRequest());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (Exception e){
            System.out.println("Reader Thread interrupted");
            e.printStackTrace();
        }
    }

    /*
    *
    *  PREGAME
    *
    * */

    /* JOIN TO GAME ON ENTERED PORT NUMBER */
    public void onClickJoin(ActionEvent event){
        try{
            clientMessageStream = new ClientMessageStream(new TCPMessageStream(
                    new Socket(hostField.getText(), Integer.parseInt(portField.getText()))));
            Thread readerTh = new Thread(this::readerThread);
            readerTh.start();

            portNumber = Integer.parseInt(portField.getText());
            pregameBox.getChildren().clear();
            pregameBox.getChildren().add(lblPort);
            lblPort.setText("Waiting for other players to join");
        }
        catch(Exception e){
            lblPort.setText("Port not reachable, try again:");
            return;
        }
        joinGame();
    }

    /* SETTING A SERVER AND JOINING GAME */
    public void onClickSetServer(ActionEvent event) {
        server = new Server();
        server.setVerbose(true);
        server.runInNewThread();
        pregameBox.getChildren().clear();
        try{
            clientMessageStream = new ClientMessageStream(new TCPMessageStream(
                    new Socket(hostField.getText(), server.getPort())));
            Thread readerTh = new Thread(this::readerThread);
            readerTh.start();

            lblPort.setText("Waiting for other players to join\n\nPORT:   " + server.getPort());
            portNumber = server.getPort();
        }
        catch(Exception e){
            lblPort.setText("INTERRUPTED");
        }
        pregameBox.getChildren().add(lblPort);
        joinGame();
    }
    /* JOIN GAME */
    private void joinGame(){
        debugMode = debugModeToggle.isSelected();
        String name = playerNameField.getText();
        Player.Position position = positionComboBox.getValue();
        try {
            clientMessageStream.writeMessage(new JoinGameRequest(name, position));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            clientMessageStream.writeMessage(new StateRequest());
        } catch (Exception e){
            throw new RuntimeException(e);
        };
    }

    /*
     *
     *  BIDDING
     *
     * */

    /* BIDDING AND CHECKING IF END OF BIDDING */
    public void onBidButtonClicked(ActionEvent event, Bidding.Bid bid) {
        if (game.getBidding().canBid(game.getCurrentTurn(), bid)){
            try {
                clientMessageStream.writeMessage(new MakeBidRequest(game.getCurrentTurn(), bid));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* UPDATE THE GRID TO SHOW ONLY AVAILABLE BIDS */
    public void updateBiddingGridColors() {
        List<Suit> suits = new ArrayList<>(List.of(Suit.values()[1], Suit.values()[0], Suit.values()[2], Suit.values()[3]));
        suits.add(Suit.NO_TRUMP);
        for (int level = 1; level <= 7; level++) {
            for (int col = 0; col < suits.size(); col++) {
                Bidding.Bid bid = new Bidding.Bid(level, suits.get(col));
                Button button = (Button) biddingGrid.getChildren().get((level - 1) * suits.size() + col);
                button.setVisible(game.getBidding().getAvailableBids().contains(bid));
            }
        }
        int col = 0;
        for (Bidding.Bid.SpecialBid specialBid : Bidding.Bid.SpecialBid.values()) {
            Bidding.Bid bid = new Bidding.Bid(0, null, specialBid);
            Button button = (Button) biddingGrid.getChildren().get(7 * suits.size() + col);
            button.setVisible(game.getBidding().getAvailableBids().contains(bid));
            col++;
        }
    }

    /* MAKING BIGGER LABEL FOR ACTUAL BIDING PLAYER */
    public void makeTurn(Player.Position position) {
        for (int i = 0; i < labels.size(); i++) {
            if (Player.Position.values()[i].toString().equals(position.toString())) {
                labels.get(i).setStyle("-fx-font-size: 16; -fx-font-weight: bold");
            }
            else
                labels.get(i).setStyle("-fx-font-weight: normal; -fx-font-size: 12");
        }
    }

    /*
     *
     *  PLAYING
     *
     * */

    /* MAKING BIGGER LABEL FOR ACTUAL PLAYING PLAYER,
    SHOWING AVAILABLE CARDS AND COUNTING GAINED TRICKS */
    public void makeTurnPlaying(Player.Position position) {
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
        }
        for (int i = 0; i < 4; i++) {
            for (Card card : game.getDeck().deal().get(i).getCards()){
                if ((myPosition.ordinal() == i || game.getContract().getDeclarer() == myPosition && game.getContract().getDummy().ordinal() == i)
                        && game.canPlayCard(card)
                        && game.getNumberOfPlayedCards() % 4 != 0) {
                    switch (game.getCurrentTurn().ordinal()){
                        case 0:
                            cardImages.get(card).setTranslateY(20);
                            break;
                        case 1:
                            cardImages.get(card).setTranslateX(-20);
                            break;
                        case 2:
                            cardImages.get(card).setTranslateY(-20);
                            break;
                        case 3:
                            cardImages.get(card).setTranslateX(20);
                            break;
                    }
                }
                else if (cardImages.get(card).getParent() != null && !cardImages.get(card).getParent().equals(table)){
                    if (i % 2 == 0)
                        cardImages.get(card).setTranslateY(0);
                    else
                        cardImages.get(card).setTranslateX(0);
                }
            }
        }
        makeTurn(position);
    }

    /* PLYING CARDS AND CHECKING IF FINISHED */
    public void onCardClicked(MouseEvent event, Card card, int position) {
        Player.Position pos = Player.Position.values()[position];
        if (pos == myPosition
                || myPosition == game.getContract().getDeclarer() && pos == game.getContract().getDummy()
        ) {
            try {
                clientMessageStream.writeMessage(new PlayCardRequest(pos, card));
            } catch (IOException e) {
                System.out.println("IO Exception in PlayingController.onCardClicked");
                throw new RuntimeException(e);
            }
        }
    }

    /* PLAY RANDOM CARDS TO DEBUG */
    public void onDebugButtonClicked(ActionEvent event) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            Player.Position pos = game.getCurrentTurn();
            for (Card card : game.getDeck().deal().get(pos.ordinal()).getCards()) {
                if (game.canPlayCard(card)) {
                    clientMessageStream.writeMessage(new PlayCardRequest(pos, card));
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* SCOREBOARD */
    public void onClickScoreboard(ActionEvent event) {
        ScoreboardView view = new ScoreboardView(scoringEntryList);

        Stage scoreboardStage = new Stage();
        scoreboardStage.setTitle("Scoreboard");
        scoreboardStage.setScene(new Scene(view));
        scoreboardStage.show();
    }


    /*
     *
     *  FINISHED
     *
     * */

    public void onPlayNextRound(ActionEvent event) {
        try {
            clientMessageStream.writeMessage(new NewGameRequest());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /* CHANGE STATE OF GAME */
    private void startBidding() {
        BiddingView view = new BiddingView(this);

        stage.setTitle("TCS Bridge - BIDDING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }

    private void startPlaying(){
        PlayingView view = new PlayingView(this);

        stage.setTitle("TCS Bridge - PLAYING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
        table.getChildren().clear();
        inforamtionLeftLabel.setText(inforamtionLeftLabel.getText() + "\n" + game.getContract().getDeclarer().name());
    }

    public void startFinished(){
        FinishedView view = new FinishedView(this, new ScoreboardView(scoringEntryList));

        stage.setTitle("TCS Bridge - FINISHED");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
