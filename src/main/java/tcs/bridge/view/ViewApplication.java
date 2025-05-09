package tcs.bridge.view;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tcs.bridge.model.*;

import java.util.List;

public class ViewApplication extends Application {

    public Scene makeTable(List<Hand> deal){
        // Setting a scene with
        BorderPane deck = new BorderPane();

        for (int i = 0; i < deal.size(); i++) {
            StackPane north = new StackPane();
            north.setAlignment(Pos.CENTER);
            int translate = -283;
            for (Card card : deal.get(0).getCards()) {
                ImageView imageView = new ImageView(String.valueOf(ViewApplication.class.
                        getResource("/tcs/bridge/view/cards/" +
                                card.getSuit().getName().toLowerCase() + "_" + card.getRank().getName() + ".png")));
                imageView.setFitWidth(96.8);
                imageView.setFitHeight(136);
                if (i == 0 || i == 2)
                    imageView.setTranslateX(translate += 40);
                else
                    imageView.setTranslateY(translate += 40);
                imageView.getOnMouseClicked();
                north.getChildren().add(imageView);
            }
            switch (i){
                case 0: deck.setTop(north); break;
                case 1: deck.setRight(north); break;
                case 2: deck.setBottom(north); break;
                case 3: deck.setLeft(north); break;
            }
        }

        deck.setCenter(new Button("Table"));
        deck.setStyle("-fx-background-color: #32442d;");

        //Creating a scene object
        return new Scene(deck, 900, 900);
    }

    @Override
    public void start(Stage stage) throws Exception {
        //Instantiating the BorderPane class
        Deck deck = new Deck();

        Scene scene = makeTable(deck.shuffle().deal());

        stage.setTitle("Deck View");
        stage.setScene(scene);
        stage.show();
    }

    // testowe
    public static void main(String[] args) {launch();}
}
