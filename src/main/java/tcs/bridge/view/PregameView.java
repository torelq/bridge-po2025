package tcs.bridge.view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import tcs.bridge.controller.Controller;
import tcs.bridge.model.*;


public class PregameView extends StackPane {

    public PregameView(Controller controller){
        this.setStyle("-fx-background-color: #32442d;");

        Button joinButton = new Button("Join");
        controller.portField = new TextField();
        controller.playerNameField = new TextField("Name");
        controller.hostField = new TextField("127.0.0.1");
        controller.lblPort = new Label("Port:");
        controller.lblPort.setStyle("-fx-text-fill: beige;");
        controller.lblHost = new Label("Host:");
        controller.lblHost.setStyle("-fx-text-fill: beige;");
        Button setServer = new Button("Set Server");
        controller.positionComboBox = new ComboBox<>();
        controller.positionComboBox.setPromptText("Position");
        controller.positionComboBox.getItems().addAll(Player.Position.values());
        joinButton.setOnAction(controller::onClickJoin);
        setServer.setOnAction(controller::onClickSetServer);
        controller.debugModeToggle = new ToggleButton("Debug Mode OFF");
        controller.debugModeToggle.setOnAction(event -> {
            if (controller.debugModeToggle.isSelected()) {
                controller.debugModeToggle.setText("Debug Mode ON");
                controller.debugModeToggle.setStyle("-fx-font-weight: bold");
            } else {
                controller.debugModeToggle.setText("Debug Mode OFF");
                controller.debugModeToggle.setStyle("-fx-font-weight: normal");
            }
        });

        VBox labels = new VBox(15, controller.lblHost, controller.lblPort);
        VBox textBox = new VBox(10, controller.hostField, controller.portField);
        VBox namePositionBox = new VBox(10, controller.positionComboBox, controller.playerNameField);
        textBox.setAlignment(Pos.CENTER);
        labels.setAlignment(Pos.CENTER);
        namePositionBox.setAlignment(Pos.CENTER);

        controller.pregameBox = new HBox(labels, textBox,
                namePositionBox, controller.debugModeToggle, joinButton, setServer);
        controller.pregameBox.setAlignment(Pos.CENTER);
        controller.pregameBox.setSpacing(10);
        this.getChildren().add(controller.pregameBox);
    }
}
