package application;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReceptionistPage {

    private BorderPane view;
    private Stage primaryStage;

    // TableView for appointments
    private TableView<Appointment> appointmentTable = new TableView<>();
    // TableView for doctors
    private TableView<Doctor> doctorTable = new TableView<>();

    public ReceptionistPage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        // Navigation Bar
        Button registerPatientButton = new Button("Register Patient");
        Button viewAppointmentsButton = new Button("View Appointments");
        Button manageDoctorsButton = new Button("Manage Doctors");

        VBox navBar = new VBox(10, registerPatientButton, viewAppointmentsButton, manageDoctorsButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        // Main Content
        Label titleLabel = new Label("Welcome, Receptionist");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        // Initialize Tables
        initializeAppointmentTable();
        initializeDoctorTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable, doctorTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        // Root Layout
        view = new BorderPane();
        view.setLeft(navBar);
        view.setCenter(mainContent);

        // Button Actions
        registerPatientButton.setOnAction(e -> {
            // Open a new window for registering a patient
            openRegisterPatientWindow();
        });

        viewAppointmentsButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM appointments");
                ObservableList<Appointment> appointments = FXCollections.observableArrayList();
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("id"),
                        rs.getString("patient_username"),
                        rs.getString("doctor_username"),
                        rs.getString("date")
                    ));
                }
                appointmentTable.setItems(appointments);
                doctorTable.setVisible(false); // Hide doctors table
                appointmentTable.setVisible(true); // Show appointments table
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        manageDoctorsButton.setOnAction(e -> {
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
                appointmentTable.setVisible(false); // Hide appointments table
                doctorTable.setVisible(true); // Show doctors table
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Open a new window for registering a patient.
     */
    private void openRegisterPatientWindow() {
        Stage registerPatientStage = new Stage();
        registerPatientStage.setTitle("Register Patient");

        // Layout
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25, 25, 25, 25));

        // Name
        Label nameLabel = new Label("Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter patient's name");

        // Email
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter patient's email");

        // Password
        Label passwordLabel = new Label("Password:");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Enter patient's password");

        // Submit Button
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Add components to layout
        layout.getChildren().addAll(nameLabel, nameField, emailLabel, emailField, passwordLabel, passwordField, submitButton);

        // Submit Button Action
        submitButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                System.out.println("Please fill all fields.");
            } else {
                try {
                    Database.executeUpdate("INSERT INTO users (username, password, role, name, email) VALUES (?, ?, 'Patient', ?, ?)",
                            email, password, name, email);
                    System.out.println("Patient registered successfully!");
                    registerPatientStage.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to register patient.");
                }
            }
        });

        // Scene
        Scene scene = new Scene(layout, 300, 250);
        registerPatientStage.setScene(scene);
        registerPatientStage.show();
    }

    /**
     * Initialize the Appointments Table.
     */
    private void initializeAppointmentTable() {
        TableColumn<Appointment, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Appointment, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<Appointment, String> doctorColumn = new TableColumn<>("Doctor");
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorUsername"));

        TableColumn<Appointment, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        appointmentTable.getColumns().addAll(idColumn, patientColumn, doctorColumn, dateColumn);
        appointmentTable.setVisible(false); // Initially hidden
    }

    /**
     * Initialize the Doctors Table.
     */
    private void initializeDoctorTable() {
        TableColumn<Doctor, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Doctor, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Doctor, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Doctor, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        doctorTable.getColumns().addAll(idColumn, usernameColumn, nameColumn, emailColumn);
        doctorTable.setVisible(false); // Initially hidden
    }

    public BorderPane getView() {
        return view;
    }

    /**
     * Model class for Appointments.
     */
    public static class Appointment {
        private final int id;
        private final String patientUsername;
        private final String doctorUsername;
        private final String date;

        public Appointment(int id, String patientUsername, String doctorUsername, String date) {
            this.id = id;
            this.patientUsername = patientUsername;
            this.doctorUsername = doctorUsername;
            this.date = date;
        }

        public int getId() {
            return id;
        }

        public String getPatientUsername() {
            return patientUsername;
        }

        public String getDoctorUsername() {
            return doctorUsername;
        }

        public String getDate() {
            return date;
        }
    }

    /**
     * Model class for Doctors.
     */
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

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }
}