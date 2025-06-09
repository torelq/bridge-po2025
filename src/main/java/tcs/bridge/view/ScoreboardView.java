package tcs.bridge.view;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import tcs.bridge.model.Scoring;

import java.util.List;

public class ScoreboardView extends TableView<ScoreboardView.ScoringSimpleString> {
    public static class ScoringSimpleString {
        private final SimpleStringProperty contract;
        private final SimpleIntegerProperty ns;
        private final SimpleIntegerProperty ew;

        public ScoringSimpleString(String contract, int ns, int ew) {
            this.contract = new SimpleStringProperty(contract);
            this.ns = new SimpleIntegerProperty(ns);
            this.ew = new SimpleIntegerProperty(ew);
        }
        public SimpleStringProperty contractProperty() {
            return contract;
        }
        public SimpleIntegerProperty nsProperty() {
            return ns;
        }
        public SimpleIntegerProperty ewProperty() {
            return ew;
        }
    }

    public ScoreboardView(List<Scoring.ScoringEntry> scoringEntryList) {
        ObservableList<ScoringSimpleString> scoringObservableList = FXCollections.observableArrayList();
        if (scoringEntryList != null) {
            for (Scoring.ScoringEntry scoringEntry : scoringEntryList) {
                scoringObservableList.add(new ScoringSimpleString(
                        scoringEntry.getContract().toString() + "; Won tricks: " + scoringEntry.getWonTricks(),
                        scoringEntry.getNS(), scoringEntry.getEW()));
            }
        }

        TableColumn<ScoringSimpleString, String> contractColumn = new TableColumn<>("Contract");
        contractColumn.setCellValueFactory(cellData -> cellData.getValue().contractProperty());
        TableColumn<ScoringSimpleString, Number> nsColumn = new TableColumn<>("NS");
        nsColumn.setCellValueFactory(cellData -> cellData.getValue().nsProperty());
        TableColumn<ScoringSimpleString, Number> ewColumn = new TableColumn<>("EW");
        ewColumn.setCellValueFactory(cellData -> cellData.getValue().ewProperty());

        this.getColumns().addAll(contractColumn, nsColumn, ewColumn);
        this.setItems(scoringObservableList);
        this.setStyle("-fx-font-size: 15");
    }
}
