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

public class PatientPage {

    private BorderPane view;
    private Stage primaryStage;
    private String username;

    // TableView for appointments
    private TableView<Appointment> appointmentTable = new TableView<>();
    // TableView for medical history
    private TableView<MedicalRecord> medicalHistoryTable = new TableView<>();

    public PatientPage(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        this.username = username;
        createView();
    }

    private void createView() {
        // Navigation Bar
        Button viewAppointmentsButton = new Button("View Appointments");
        Button bookAppointmentButton = new Button("Book Appointment");
        Button viewMedicalHistoryButton = new Button("View Medical History");

        VBox navBar = new VBox(10, viewAppointmentsButton, bookAppointmentButton, viewMedicalHistoryButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        // Main Content
        Label titleLabel = new Label("Welcome, Patient: " + username);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        // Initialize Tables
        initializeAppointmentTable();
        initializeMedicalHistoryTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable, medicalHistoryTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        // Root Layout
        view = new BorderPane();
        view.setLeft(navBar);
        view.setCenter(mainContent);

        // Button Actions
        viewAppointmentsButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM appointments WHERE patient_username = ?", username);
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
                medicalHistoryTable.setVisible(false); // Hide medical history table
                appointmentTable.setVisible(true); // Show appointments table
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        bookAppointmentButton.setOnAction(e -> {
            // Open a new window for booking an appointment
            openBookAppointmentWindow();
        });

        viewMedicalHistoryButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM medical_history WHERE patient_username = ?", username);
                ObservableList<MedicalRecord> history = FXCollections.observableArrayList();
                while (rs.next()) {
                    history.add(new MedicalRecord(
                        rs.getInt("id"),
                        rs.getString("patient_username"),
                        rs.getString("record")
                    ));
                }
                medicalHistoryTable.setItems(history);
                appointmentTable.setVisible(false); // Hide appointments table
                medicalHistoryTable.setVisible(true); // Show medical history table
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    }

    /**
     * Open a new window for booking an appointment.
     */
    private void openBookAppointmentWindow() {
        Stage bookAppointmentStage = new Stage();
        bookAppointmentStage.setTitle("Book Appointment");

        // Layout
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(25, 25, 25, 25));

        // Doctor Username
        Label doctorLabel = new Label("Doctor Username:");
        TextField doctorField = new TextField();
        doctorField.setPromptText("Enter doctor username");

        // Date
        Label dateLabel = new Label("Date (YYYY-MM-DD):");
        TextField dateField = new TextField();
        dateField.setPromptText("Enter date");

        // Submit Button
        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        // Add components to layout
        layout.getChildren().addAll(doctorLabel, doctorField, dateLabel, dateField, submitButton);

        // Submit Button Action
        submitButton.setOnAction(e -> {
            String doctorUsername = doctorField.getText();
            String date = dateField.getText();

            if (doctorUsername.isEmpty() || date.isEmpty()) {
                System.out.println("Please fill all fields.");
            } else {
                try {
                    Database.executeUpdate("INSERT INTO appointments (patient_username, doctor_username, date) VALUES (?, ?, ?)",
                            username, doctorUsername, date);
                    System.out.println("Appointment booked successfully!");
                    refreshAppointments();
                    bookAppointmentStage.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to book appointment.");
                }
            }
        });

        // Scene
        Scene scene = new Scene(layout, 300, 200);
        bookAppointmentStage.setScene(scene);
        bookAppointmentStage.show();
    }

    /**
     * Refresh the appointments table.
     */
    private void refreshAppointments() {
        try {
            ResultSet rs = Database.executeQuery("SELECT * FROM appointments WHERE patient_username = ?", username);
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
     * Initialize the Medical History Table.
     */
    private void initializeMedicalHistoryTable() {
        TableColumn<MedicalRecord, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MedicalRecord, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<MedicalRecord, String> recordColumn = new TableColumn<>("Record");
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));

        medicalHistoryTable.getColumns().addAll(idColumn, patientColumn, recordColumn);
        medicalHistoryTable.setVisible(false); // Initially hidden
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
     * Model class for Medical Records.
     */
    public static class MedicalRecord {
        private final int id;
        private final String patientUsername;
        private final String record;

        public MedicalRecord(int id, String patientUsername, String record) {
            this.id = id;
            this.patientUsername = patientUsername;
            this.record = record;
        }

        public int getId() {
            return id;
        }

        public String getPatientUsername() {
            return patientUsername;
        }

        public String getRecord() {
            return record;
        }
    }
}