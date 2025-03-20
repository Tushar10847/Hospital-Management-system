package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ddd {

    private BorderPane view;
    private Stage primaryStage;

    // TableView for appointments
    private TableView<Appointment> appointmentTable = new TableView<>();

    public ddd(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        // Navigation Bar
        Button viewAppointmentsButton = new Button("View Appointments");
        Button manageScheduleButton = new Button("Manage Schedule");

        VBox navBar = new VBox(10, viewAppointmentsButton, manageScheduleButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        // Main Content
        Label titleLabel = new Label("Welcome, Doctor");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        // Initialize Tables
        initializeAppointmentTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        // Root Layout
        view = new BorderPane();
        view.setLeft(navBar);
        view.setCenter(mainContent);

        // Button Actions
        viewAppointmentsButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM appointments WHERE doctor_username = ?", "doctor1");
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
        });

        manageScheduleButton.setOnAction(e -> {
            // Open a new window for managing the schedule
            openManageScheduleWindow();
        });
    }

    private void openManageScheduleWindow() {
        // Implement schedule management logic here
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

        appointmentTable.getColumns().addAll(idColumn, patientColumn, doctorColumn, dateColumn);
    }

    public BorderPane getView() {
        return view;
    }

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
}