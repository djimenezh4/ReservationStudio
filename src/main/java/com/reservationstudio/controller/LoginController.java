package com.reservationstudio.controller;

import com.reservationstudio.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    private static final String MAIN_SCREEN_FXML = "/com/reservationstudio/main.fxml";

    @FXML private TextField UsernameBox;
    @FXML private PasswordField PasswBox;
    @FXML private Label messageLabel;

    @FXML private void handleLogin(ActionEvent event) {

        String username = UsernameBox.getText();
        String password = PasswBox.getText();

        User user = User.authenticate(username, password);

        if (user != null) {

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(MAIN_SCREEN_FXML)
                );

                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

                stage.setScene(new Scene(root));
                stage.setTitle("Dashboard");
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Error loading dashboard.");
            }

        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }
}
