module tcs.bridge {
    requires javafx.controls;
    requires javafx.fxml;


    opens tcs.bridge to javafx.fxml;
    exports tcs.bridge;
}