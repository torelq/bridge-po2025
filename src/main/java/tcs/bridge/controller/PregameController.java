package tcs.bridge.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import tcs.bridge.communication.messages.JoinGameNotice;
import tcs.bridge.communication.messages.JoinGameRequest;
import tcs.bridge.communication.messages.ServerToClientMessage;
import tcs.bridge.communication.messages.StateRequest;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.communication.streams.TCPMessageStream;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.server.Server;
import tcs.bridge.view.BiddingView;

import java.io.IOException;
import java.net.Socket;

import static tcs.bridge.App.clientMessageStream;
import static tcs.bridge.App.server;
import static tcs.bridge.App.stage;
import static tcs.bridge.App.game;
import static tcs.bridge.App.myPosition;
import static tcs.bridge.App.playerNames;


public class PregameController{
//    private Thread eventThread;

    private void readerThread() {
        try {
            while (true) {
                ServerToClientMessage message = clientMessageStream.readMessage();
                System.out.println(message);
                if (message instanceof StateRequest.StateResponse) {
                    Game serverGame = ((StateRequest.StateResponse) message).game();
                    myPosition = ((StateRequest.StateResponse) message).myPosition();
                    if (serverGame.getState() == Game.State.BIDDING){
                        System.out.println(serverGame.getState().toString());
                        Platform.runLater(() -> {startBidding(serverGame);});
                        break;
                    }
                }
                if (message instanceof JoinGameNotice){
                    JoinGameNotice joinGameNotice = ((JoinGameNotice) message);
                    playerNames.set(joinGameNotice.position().ordinal(), joinGameNotice.name());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public TextField portField;
    public TextField playerNameField;
    public Label lblPort;
    public HBox pregameBox;
    public ComboBox<Player.Position> positionComboBox;

    public void onClickJoin(ActionEvent event){
        try{
            clientMessageStream = new ClientMessageStream(new TCPMessageStream(
                    new Socket("127.0.0.1", Integer.parseInt(portField.getText()))));
            Thread readerTh = new Thread(() -> readerThread());
            readerTh.start();


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

    public void onClickSetServer(ActionEvent event) {
        server = new Server();
        server.setVerbose(true); // TODO: usunac
        server.runInNewThread();
        pregameBox.getChildren().clear();
        try{
            clientMessageStream = new ClientMessageStream(new TCPMessageStream(
                    new Socket("127.0.0.1", server.getPort())));
            Thread readerTh = new Thread(() -> readerThread());
            readerTh.start();

            lblPort.setText("Waiting for other players to join\n\nPORT:   " + server.getPort());
        }
        catch(Exception e){
            lblPort.setText("INTERRUPTED");
        }
        pregameBox.getChildren().add(lblPort);
        joinGame();
    }

    private void joinGame(){
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

    private void startBidding(Game serverGame){
        game = serverGame;
        BiddingController controller = new BiddingController();
        BiddingView view = new BiddingView(controller);

        stage.setTitle("TCS Bridge - BIDDING");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
}
