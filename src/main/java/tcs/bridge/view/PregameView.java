package tcs.bridge.view;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import tcs.bridge.controller.PregameController;
import tcs.bridge.model.*;

import java.awt.*;

public class PregameView extends StackPane {
    private final Game game;
    private final PregameController controller;

    public PregameView(Game game, PregameController controller){
        this.game = game;
        this.controller = controller;
        this.setStyle("-fx-background-color: #32442d;");

        Button joinButton = new Button("Join x 4");
        joinButton.setOnAction(controller::onClickJoin);
        this.getChildren().add(joinButton);
    }
}
