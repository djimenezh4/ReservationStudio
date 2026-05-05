package com.reservationstudio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class App extends Application {
    private static final String LOGIN_FXML = "/com/reservationstudio/Login.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(LOGIN_FXML));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1100, 760);
        primaryStage.setTitle("Reservation Studio");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
}
