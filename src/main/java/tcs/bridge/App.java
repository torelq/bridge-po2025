package tcs.bridge;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tcs.bridge.communication.streams.ClientMessageStream;
import tcs.bridge.controller.PregameController;
import tcs.bridge.model.Game;
import tcs.bridge.model.Player;
import tcs.bridge.server.Server;
import tcs.bridge.view.PregameView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    static public Server server;
    static public ClientMessageStream clientMessageStream;
    static public Game game;
    static public Stage stage;
    static public Player.Position myPosition;
    static public ArrayList<String> playerNames;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        game = new Game();
        stage = primaryStage;
        playerNames = new ArrayList<>(List.of("", "", "", ""));

        PregameController controller = new PregameController();
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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (App.server != null) {
            App.server.shutdown();
        }
    }
}
