module com.reservationstudio {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.reservationstudio to javafx.fxml;
    opens com.reservationstudio.controller to javafx.fxml;
    exports com.reservationstudio;
}
