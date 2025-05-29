package tcs.bridge.view;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import tcs.bridge.controller.PregameController;
import tcs.bridge.model.*;


public class PregameView extends StackPane {

    public PregameView(PregameController controller){
        this.setStyle("-fx-background-color: #32442d;");

        Button joinButton = new Button("Join");
        controller.portField = new TextField();
        controller.playerNameField = new TextField("Name");
        controller.lblPort = new Label("Port:");
        controller.lblPort.setStyle("-fx-text-fill: beige;");
        Button setServer = new Button("Set Server");
        controller.positionComboBox = new ComboBox<>();
        controller.positionComboBox.getItems().addAll(Player.Position.values());
        joinButton.setOnAction(controller::onClickJoin);
        setServer.setOnAction(controller::onClickSetServer);

        controller.pregameBox = new HBox(controller.lblPort, controller.portField,
                controller.positionComboBox, controller.playerNameField, joinButton, setServer);
        controller.pregameBox.setAlignment(Pos.CENTER);
        controller.pregameBox.setSpacing(10);
        this.getChildren().add(controller.pregameBox);
    }
}
