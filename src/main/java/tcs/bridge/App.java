package tcs.bridge;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tcs.bridge.controller.PregameController;
import tcs.bridge.model.Game;
import tcs.bridge.view.PregameView;

public class App extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Game game = new Game();
        PregameController controller = new PregameController(primaryStage, game);
        PregameView view = new PregameView(game, controller);

        primaryStage.setTitle("TCS Bridge - PREGAME");
        primaryStage.setScene(new Scene(view, 900, 900));
        primaryStage.show();
    }
}
