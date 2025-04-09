package application;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegistrationPage {

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hospital Management System - Register");

        StackPane root = new StackPane();
        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/application/resources/images/dd.jpg"));
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

        Label title = new Label("Create Account");
        title.getStyleClass().add("login-register-title");

        Label subtitle = new Label("Join our healthcare system");
        subtitle.getStyleClass().add("login-register-subtitle");

        header.getChildren().addAll(logo, title, subtitle);

        VBox form = new VBox(15);
        form.getStyleClass().add("login-register-form");
        form.setAlignment(Pos.CENTER);

        VBox nameContainer = new VBox();
        nameContainer.getStyleClass().add("login-register-field-container");
        Label nameLabel = new Label("Full Name");
        nameLabel.getStyleClass().add("login-register-label");
        TextField nameField = new TextField();
        nameField.getStyleClass().add("login-register-field");
        nameField.setPromptText("Enter your full name");
        nameContainer.getChildren().addAll(nameLabel, nameField);

        VBox emailContainer = new VBox();
        emailContainer.getStyleClass().add("login-register-field-container");
        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("login-register-label");
        TextField emailField = new TextField();
        emailField.getStyleClass().add("login-register-field");
        emailField.setPromptText("Enter your email");
        emailContainer.getChildren().addAll(emailLabel, emailField);

        VBox passwordContainer = new VBox();
        passwordContainer.getStyleClass().add("login-register-field-container");
        Label pwLabel = new Label("Password");
        pwLabel.getStyleClass().add("login-register-label");
        PasswordField pwBox = new PasswordField();
        pwBox.getStyleClass().add("login-register-field");
        pwBox.setPromptText("Create a password");
        passwordContainer.getChildren().addAll(pwLabel, pwBox);

        VBox roleContainer = new VBox();
        roleContainer.getStyleClass().add("login-register-field-container");
        Label roleLabel = new Label("Role");
        roleLabel.getStyleClass().add("login-register-label");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getStyleClass().add("login-register-combo");
        roleComboBox.getItems().addAll("Patient", "Doctor", "Receptionist");
        roleComboBox.setPromptText("Select your role");
        roleContainer.getChildren().addAll(roleLabel, roleComboBox);

        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("login-register-button");

        Button loginBtn = new Button("Already have an account? Login");
        loginBtn.getStyleClass().add("login-register-link");

        form.getChildren().addAll(nameContainer, emailContainer, passwordContainer, roleContainer, registerBtn, loginBtn);

        container.getChildren().addAll(header, form);
        root.getChildren().add(container);
        StackPane.setAlignment(container, Pos.CENTER);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/application/login_registration.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        registerBtn.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = pwBox.getText();
            String role = roleComboBox.getValue();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
                System.out.println("Please fill all fields and select a role.");
            } else {
                try {
                    Database.executeUpdate("INSERT INTO users (username, password, role, name, email) VALUES (?, ?, ?, ?, ?)",
                            email, password, role, name, email);
                    System.out.println("Registered: " + name + ", " + email + ", " + role);
                    LoginPage loginPage = new LoginPage();
                    loginPage.start(new Stage());
                    primaryStage.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Registration failed.");
                }
            }
        });

        loginBtn.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(new Stage());
            primaryStage.close();
        });
    }
}