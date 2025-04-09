package application;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hospital Management System - Login");

        StackPane root = new StackPane();
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/application/resources/images/ff.jpg"));
            BackgroundImage bgImage = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
            root.setBackground(new Background(bgImage));
        } catch (Exception e) {
            System.out.println("Error loading background image: " + e.getMessage());
            root.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #c3cfe2);");
        }

        VBox container = new VBox();
        container.getStyleClass().add("login-register-container");
        container.setAlignment(Pos.CENTER);

        VBox header = new VBox();
        header.getStyleClass().add("login-register-header");
        header.setAlignment(Pos.CENTER);

        Label logo = new Label("🏥 HOSPITAL");
        logo.getStyleClass().add("login-register-logo");

        Label title = new Label("Welcome Back");
        title.getStyleClass().add("login-register-title");

        Label subtitle = new Label("Sign in to your account");
        subtitle.getStyleClass().add("login-register-subtitle");

        header.getChildren().addAll(logo, title, subtitle);

        VBox form = new VBox();
        form.getStyleClass().add("login-register-form");
        form.setAlignment(Pos.CENTER);

        VBox usernameContainer = new VBox();
        usernameContainer.getStyleClass().add("login-register-field-container");
        Label userName = new Label("Username");
        userName.getStyleClass().add("login-register-label");
        TextField userTextField = new TextField();
        userTextField.getStyleClass().add("login-register-field");
        userTextField.setPromptText("Enter your username");
        usernameContainer.getChildren().addAll(userName, userTextField);

        VBox passwordContainer = new VBox();
        passwordContainer.getStyleClass().add("login-register-field-container");
        Label pw = new Label("Password");
        pw.getStyleClass().add("login-register-label");
        PasswordField pwBox = new PasswordField();
        pwBox.getStyleClass().add("login-register-field");
        pwBox.setPromptText("Enter your password");
        passwordContainer.getChildren().addAll(pw, pwBox);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-register-error");

        HBox buttonContainer = new HBox(15);
        buttonContainer.getStyleClass().add("login-register-button-container");
        buttonContainer.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("login-register-button");

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("login-register-link");

        buttonContainer.getChildren().addAll(loginBtn, registerBtn);

        form.getChildren().addAll(usernameContainer, passwordContainer, errorLabel, buttonContainer);

        container.getChildren().addAll(header, form);
        root.getChildren().add(container);
        StackPane.setAlignment(container, Pos.CENTER);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/application/login_registration.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        loginBtn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both username and password.");
            } else if (isValidCredentials(username, password)) {
                errorLabel.setText("");
                navigateToRolePage(username, primaryStage);
            } else {
                errorLabel.setText("Invalid credentials.");
            }
        });

        registerBtn.setOnAction(e -> {
            RegistrationPage registrationPage = new RegistrationPage();
            registrationPage.start(new Stage());
            primaryStage.close();
        });
    }

    private boolean isValidCredentials(String username, String password) {
        try {
            ResultSet rs = Database.executeQuery("SELECT * FROM users WHERE username = ? AND password = ?", username, password);
            return rs.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void navigateToRolePage(String username, Stage currentStage) {
        Stage stage = new Stage();
        try {
            ResultSet rs = Database.executeQuery("SELECT role FROM users WHERE username = ?", username);
            if (rs.next()) {
                String role = rs.getString("role");
                switch (role) {
                    case "Admin":
                        AdminPage adminPage = new AdminPage();
                        adminPage.start(stage);
                        break;
                    case "Doctor":
                        DoctorPage doctorPage = new DoctorPage(stage, username); // Pass username to DoctorPage
                        stage.setScene(new Scene(doctorPage.getView(), 800, 600));
                        stage.setTitle("Doctor Dashboard - " + username);
                        stage.show();
                        break;
                    case "Patient":
                        PatientPage patientPage = new PatientPage(stage, username);
                        stage.setScene(new Scene(patientPage.getView(), 800, 600));
                        stage.setTitle("Patient Dashboard - " + username);
                        stage.show();
                        break;
                    case "Receptionist":
                        ReceptionistPage receptionistPage = new ReceptionistPage(stage);
                        stage.setScene(new Scene(receptionistPage.getView(), 800, 600));
                        stage.setTitle("Receptionist Dashboard");
                        stage.show();
                        break;
                    default:
                        System.out.println("Unknown role: " + role);
                        return;
                }
                currentStage.close();
            } else {
                System.out.println("User not found in database.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to determine user role: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}