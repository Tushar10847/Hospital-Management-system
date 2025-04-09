package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReceptionistPage {

    private BorderPane view;
    private Stage primaryStage;
    private TableView<Appointment> appointmentTable = new TableView<>();
    private TableView<Doctor> doctorTable = new TableView<>();

    public ReceptionistPage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        Button registerPatientButton = new Button("Register Patient");
        Button viewAppointmentsButton = new Button("View Appointments");
        Button manageDoctorsButton = new Button("Manage Doctors");
        Button logoutButton = new Button("Logout");

        VBox navBar = new VBox(10, registerPatientButton, viewAppointmentsButton, manageDoctorsButton, logoutButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        Label titleLabel = new Label("Welcome, Receptionist");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        initializeAppointmentTable();
        initializeDoctorTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable, doctorTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        view = new BorderPane();
        view.setLeft(navBar);
        view.setCenter(mainContent);

        registerPatientButton.setOnAction(e -> openRegisterPatientWindow());
        viewAppointmentsButton.setOnAction(e -> refreshAppointments());
        manageDoctorsButton.setOnAction(e -> refreshDoctors());
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(new Stage());
            primaryStage.close();
        });
    }

    private void openRegisterPatientWindow() {
        Stage registerPatientStage = new Stage();
        registerPatientStage.setTitle("Register Patient");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label nameLabel = new Label("Name:");
        grid.add(nameLabel, 0, 0);
        TextField nameField = new TextField();
        nameField.setPromptText("Enter patient's name");
        grid.add(nameField, 1, 0);

        Label emailLabel = new Label("Email (Username):");
        grid.add(emailLabel, 0, 1);
        TextField emailField = new TextField();
        emailField.setPromptText("Enter patient's email");
        grid.add(emailField, 1, 1);

        Label passwordLabel = new Label("Password:");
        grid.add(passwordLabel, 0, 2);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter patient's password");
        grid.add(passwordField, 1, 2);

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        grid.add(submitButton, 1, 3);

        Label statusLabel = new Label();
        grid.add(statusLabel, 0, 4, 2, 1);

        submitButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Please fill all fields.");
            } else if (userExists(email)) {
                statusLabel.setText("Email (username) already exists.");
            } else {
                try {
                    Database.executeUpdate(
                        "INSERT INTO users (username, password, role, name, email) VALUES (?, ?, 'Patient', ?, ?)",
                        email, password, name, email
                    );
                    statusLabel.setText("Patient registered successfully!");
                    nameField.clear();
                    emailField.clear();
                    passwordField.clear();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Failed to register patient: " + ex.getMessage());
                }
            }
        });

        Scene scene = new Scene(grid, 400, 250);
        registerPatientStage.setScene(scene);
        registerPatientStage.show();
    }

    private void refreshAppointments() {
        try {
            ResultSet rs = Database.executeQuery("SELECT * FROM appointments");
            ObservableList<Appointment> appointments = FXCollections.observableArrayList();
            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("id"),
                    rs.getString("patient_username"),
                    rs.getString("doctor_username"),
                    rs.getString("date"),
                    rs.getString("time")
                ));
            }
            appointmentTable.setItems(appointments);
            doctorTable.setVisible(false);
            appointmentTable.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to load appointments: " + ex.getMessage());
        }
    }

    private void refreshDoctors() {
        try {
            ResultSet rs = Database.executeQuery("SELECT * FROM users WHERE role = 'Doctor'");
            ObservableList<Doctor> doctors = FXCollections.observableArrayList();
            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }
            doctorTable.setItems(doctors);
            appointmentTable.setVisible(false);
            doctorTable.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to load doctors: " + ex.getMessage());
        }
    }

    private void initializeAppointmentTable() {
        TableColumn<Appointment, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Appointment, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<Appointment, String> doctorColumn = new TableColumn<>("Doctor");
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorUsername"));

        TableColumn<Appointment, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Appointment, String> timeColumn = new TableColumn<>("Time");
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));

        appointmentTable.getColumns().setAll(idColumn, patientColumn, doctorColumn, dateColumn, timeColumn);
        appointmentTable.setVisible(false);
    }

    private void initializeDoctorTable() {
        TableColumn<Doctor, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Doctor, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Doctor, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Doctor, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        doctorTable.getColumns().setAll(idColumn, usernameColumn, nameColumn, emailColumn);
        doctorTable.setVisible(false);
    }

    private boolean userExists(String username) {
        try {
            ResultSet rs = Database.executeQuery("SELECT COUNT(*) FROM users WHERE username = ?", username);
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public BorderPane getView() {
        return view;
    }

    public static class Appointment {
        private final int id;
        private final String patientUsername;
        private final String doctorUsername;
        private final String date;
        private final String time;

        public Appointment(int id, String patientUsername, String doctorUsername, String date, String time) {
            this.id = id;
            this.patientUsername = patientUsername;
            this.doctorUsername = doctorUsername;
            this.date = date;
            this.time = time;
        }

        public int getId() { return id; }
        public String getPatientUsername() { return patientUsername; }
        public String getDoctorUsername() { return doctorUsername; }
        public String getDate() { return date; }
        public String getTime() { return time; }
    }

    public static class Doctor {
        private final int id;
        private final String username;
        private final String name;
        private final String email;

        public Doctor(int id, String username, String name, String email) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.email = email;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}