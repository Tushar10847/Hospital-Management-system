package application;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login Page");

        // Layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Title
        Text title = new Text("Welcome");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 24));
        grid.add(title, 0, 0, 2, 1);

        // Username
        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);
        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        // Password
        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        // Error Message Label
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        grid.add(errorLabel, 1, 3);

        // Login Button
        Button loginBtn = new Button("Login");
        grid.add(loginBtn, 1, 4);

        // Register Button
        Button registerBtn = new Button("Register");
        grid.add(registerBtn, 1, 5);

        // Action for Login Button
        loginBtn.setOnAction(e -> {
            String username = userTextField.getText().trim(); // Trim username
            String password = pwBox.getText().trim(); // Trim password

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both username and password.");
            } else {
                try {
                    String query = "SELECT * FROM users WHERE username = ? AND password = ?";
                    System.out.println("Executing query: " + query);
                    System.out.println("Username: " + username);
                    System.out.println("Password: " + password);

                    ResultSet rs = Database.executeQuery(query, username, password);

                    if (rs.next()) {
                        System.out.println("User found: " + rs.getString("username"));
                        // Show success pop-up
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Login Successful");
                        alert.setHeaderText(null);
                        alert.setContentText("You have logged in successfully!");
                        alert.showAndWait();

                        // Navigate to the appropriate dashboard
                        navigateToRolePage(username);
                    } else {
                        errorLabel.setText("Invalid credentials");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    errorLabel.setText("Database error. Please try again.");
                }
            }
        });

        // Action for Register Button
        registerBtn.setOnAction(e -> {
            // Open the Registration Page
            RegistrationPage registrationPage = new RegistrationPage();
            registrationPage.start(new Stage());
            primaryStage.close(); // Close the login window
        });

        // Scene
        Scene scene = new Scene(grid, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Navigate to the appropriate page based on the user's role.
     */
    private void navigateToRolePage(String username) {
        try {
            String query = "SELECT role FROM users WHERE username = ?";
            ResultSet rs = Database.executeQuery(query, username);

            if (rs.next()) {
                String role = rs.getString("role");
                System.out.println("User role: " + role);

                switch (role) {
                    case "Admin":
                        AdminPage adminPage = new AdminPage();
                        adminPage.start(new Stage());
                        break;
                    case "Doctor":
                        DoctorPage doctorPage = new DoctorPage(new Stage());
                        new Stage().setScene(new Scene(doctorPage.getView(), 800, 600));
                        break;
                    case "Patient":
                        PatientPage patientPage = new PatientPage(new Stage(), username);
                        new Stage().setScene(new Scene(patientPage.getView(), 800, 600));
                        break;
                    case "Receptionist":
                        ReceptionistPage receptionistPage = new ReceptionistPage(new Stage());
                        new Stage().setScene(new Scene(receptionistPage.getView(), 800, 600));
                        break;
                    default:
                        System.out.println("Unknown role: " + role);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}