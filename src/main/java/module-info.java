module tcs.bridge {
    requires javafx.controls;
    requires javafx.fxml;


    opens tcs.bridge.controller to javafx.fxml;
    exports tcs.bridge.app;
}