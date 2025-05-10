module tcs.bridge {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens tcs.bridge.controller to javafx.fxml;
    exports tcs.bridge.app;
    exports tcs.bridge.model;

    opens tcs.bridge.view to javafx.fxml;
    exports tcs.bridge.view;
}