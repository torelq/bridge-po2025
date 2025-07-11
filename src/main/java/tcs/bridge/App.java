package tcs.bridge;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.controller.Controller;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.model.Scoring;
import tcs.bridge.server.Server;
import tcs.bridge.view.PregameView;

public class App extends Application {
    static public Server server;
    static public ClientMessageStream clientMessageStream;
    static public Game game;
    static public Stage stage;
    static public Player.Position myPosition;
    static public ArrayList<String> playerNames;
    static public int portNumber;
    static public boolean debugMode = false;
    static public List<Scoring.ScoringEntry> scoringEntryList;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        playerNames = new ArrayList<>(List.of("", "", "", ""));

        Controller controller = new Controller();
        PregameView view = new PregameView(controller);

        stage.setTitle("TCS Bridge - PREGAME");
        stage.setScene(new Scene(view, 900, 900));
        stage.show();
    }
    @Override
    public void stop() {
        System.out.println("Shutting down application");
        if (clientMessageStream != null) {
            try {
                clientMessageStream.close();
                Thread.sleep(100);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (App.server != null) {
            App.server.shutdown();
        }
    }

    public Player.Position getMyPosition() {
        return myPosition;
    }
}
