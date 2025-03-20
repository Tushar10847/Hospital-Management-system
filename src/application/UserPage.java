package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class UserPage {

    public void start(Stage primaryStage) {
        primaryStage.setTitle("User Page");

        Label label = new Label("Welcome, User!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}