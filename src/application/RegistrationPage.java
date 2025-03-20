package application;

import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

public class RegistrationPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Registration Page");

        // Layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Title
        Text title = new Text("Register");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 24));
        grid.add(title, 0, 0, 2, 1);

        // Name
        Label nameLabel = new Label("Name:");
        grid.add(nameLabel, 0, 1);
        TextField nameField = new TextField();
        grid.add(nameField, 1, 1);

        // Email
        Label emailLabel = new Label("Email:");
        grid.add(emailLabel, 0, 2);
        TextField emailField = new TextField();
        grid.add(emailField, 1, 2);

        // Password
        Label pwLabel = new Label("Password:");
        grid.add(pwLabel, 0, 3);
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 3);

        // Role
        Label roleLabel = new Label("Role:");
        grid.add(roleLabel, 0, 4);
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Admin", "Doctor", "Patient", "Receptionist");
        roleComboBox.setPromptText("Select Role");
        grid.add(roleComboBox, 1, 4);

        // Register Button
        Button registerBtn = new Button("Register");
        grid.add(registerBtn, 1, 5);

        // Error Message Label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        grid.add(errorLabel, 1, 6);

        // Action for Register Button
        registerBtn.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = pwBox.getText();
            String role = roleComboBox.getValue();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
                errorLabel.setText("Please fill all fields and select a role.");
            } else {
                try {
                    // Insert into database
                    String query = "INSERT INTO users (username, password, role, name, email) VALUES (?, ?, ?, ?, ?)";
                    Database.executeUpdate(query, email, password, role, name, email);

                    // Show success pop-up
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Registration Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("You have been registered successfully!");
                    alert.showAndWait();

                    // Redirect to Login Page
                    LoginPage loginPage = new LoginPage();
                    loginPage.start(new Stage());
                    primaryStage.close(); // Close the registration window
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    errorLabel.setText("Registration failed: " + ex.getMessage()); // Show detailed error message
                }
            }
        });

        // Scene
        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}