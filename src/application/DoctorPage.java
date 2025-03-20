package application;

import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.text.TextAlignment;

public class DoctorPage {

    private BorderPane view;
    private Stage primaryStage;

    // TableView for appointments
    private TableView<Appointment> appointmentTable = new TableView<>();
    // TableView for patient history
    private TableView<MedicalRecord> patientHistoryTable = new TableView<>();

    public DoctorPage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        // Navigation Bar
        Button viewAppointmentsButton = new Button("View Appointments");
        Button viewPatientHistoryButton = new Button("View Patient History");
        Button manageScheduleButton = new Button("Manage Schedule");

        VBox navBar = new VBox(10, viewAppointmentsButton, viewPatientHistoryButton, manageScheduleButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        // Main Content
        Label titleLabel = new Label("Welcome, Doctor");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        // Initialize Tables
        initializeAppointmentTable();
        initializePatientHistoryTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable, patientHistoryTable);
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
                patientHistoryTable.setVisible(false); // Hide patient history table
                appointmentTable.setVisible(true); // Show appointments table
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        viewPatientHistoryButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM medical_history WHERE patient_username = ?", "patient1");
                ObservableList<MedicalRecord> history = FXCollections.observableArrayList();
                while (rs.next()) {
                    history.add(new MedicalRecord(
                        rs.getInt("id"),
                        rs.getString("patient_username"),
                        rs.getString("record")
                    ));
                }
                patientHistoryTable.setItems(history);
                appointmentTable.setVisible(false); // Hide appointments table
                patientHistoryTable.setVisible(true); // Show patient history table
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        manageScheduleButton.setOnAction(e -> {
            // Open a new window for managing the schedule
            openManageScheduleWindow();
        });
    }

    /**
     * Open a new window for managing the schedule.
     */
    private void openManageScheduleWindow() {
        Stage manageScheduleStage = new Stage();
        manageScheduleStage.setTitle("Manage Schedule");

        // Layout
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Patient Username
        Label patientLabel = new Label("Patient Username:");
        grid.add(patientLabel, 0, 0);
        TextField patientField = new TextField();
        grid.add(patientField, 1, 0);

        // Date
        Label dateLabel = new Label("Date (YYYY-MM-DD):");
        grid.add(dateLabel, 0, 1);
        TextField dateField = new TextField();
        grid.add(dateField, 1, 1);

        // Buttons
        Button addButton = new Button("Add Appointment");
        Button updateButton = new Button("Update Appointment");
        Button deleteButton = new Button("Delete Appointment");

        grid.add(addButton, 0, 2);
        grid.add(updateButton, 1, 2);
        grid.add(deleteButton, 2, 2);

        // Add Appointment Action
        addButton.setOnAction(e -> {
            String patientUsername = patientField.getText();
            String date = dateField.getText();

            if (patientUsername.isEmpty() || date.isEmpty()) {
                System.out.println("Please fill all fields.");
            } else {
                try {
                    Database.executeUpdate("INSERT INTO appointments (patient_username, doctor_username, date) VALUES (?, ?, ?)",
                            patientUsername, "doctor1", date);
                    System.out.println("Appointment added successfully!");
                    refreshAppointments();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to add appointment.");
                }
            }
        });

        // Update Appointment Action
        updateButton.setOnAction(e -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
            if (selectedAppointment == null) {
                System.out.println("Please select an appointment to update.");
            } else {
                String patientUsername = patientField.getText();
                String date = dateField.getText();

                if (patientUsername.isEmpty() || date.isEmpty()) {
                    System.out.println("Please fill all fields.");
                } else {
                    try {
                        Database.executeUpdate("UPDATE appointments SET patient_username = ?, date = ? WHERE id = ?",
                                patientUsername, date, selectedAppointment.getId());
                        System.out.println("Appointment updated successfully!");
                        refreshAppointments();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to update appointment.");
                    }
                }
            }
        });

        // Delete Appointment Action
        deleteButton.setOnAction(e -> {
            Appointment selectedAppointment = appointmentTable.getSelectionModel().getSelectedItem();
            if (selectedAppointment == null) {
                System.out.println("Please select an appointment to delete.");
            } else {
                try {
                    Database.executeUpdate("DELETE FROM appointments WHERE id = ?", selectedAppointment.getId());
                    System.out.println("Appointment deleted successfully!");
                    refreshAppointments();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to delete appointment.");
                }
            }
        });

        // Scene
        Scene scene = new Scene(grid, 400, 200);
        manageScheduleStage.setScene(scene);
        manageScheduleStage.show();
    }

    /**
     * Refresh the appointments table.
     */
    private void refreshAppointments() {
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
     * Initialize the Patient History Table.
     */
    private void initializePatientHistoryTable() {
        TableColumn<MedicalRecord, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MedicalRecord, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<MedicalRecord, String> recordColumn = new TableColumn<>("Record");
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));

        // Enable text wrapping for the Record column
        recordColumn.setCellFactory(tc -> {
            TableCell<MedicalRecord, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setWrapText(true);
                        setTextAlignment(TextAlignment.LEFT);
                    }
                }
            };
            return cell;
        });

        // Set preferred width for columns
        idColumn.setPrefWidth(50);
        patientColumn.setPrefWidth(100);
        recordColumn.setPrefWidth(400); // Wider column for records

        patientHistoryTable.getColumns().addAll(idColumn, patientColumn, recordColumn);
        patientHistoryTable.setVisible(false); // Initially hidden

        // Adjust row height to fit wrapped text
        patientHistoryTable.setRowFactory(tv -> {
            TableRow<MedicalRecord> row = new TableRow<>();
            row.setPrefHeight(50); // Adjust row height as needed
            return row;
        });
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