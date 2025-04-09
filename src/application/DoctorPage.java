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

public class DoctorPage {

    private BorderPane view;
    private Stage primaryStage;
    private String doctorUsername;
    private TableView<Appointment> appointmentTable = new TableView<>();
    private TableView<MedicalRecord> patientHistoryTable = new TableView<>();

    public DoctorPage(Stage primaryStage, String doctorUsername) {
        this.primaryStage = primaryStage;
        this.doctorUsername = doctorUsername;
        createView();
    }

    private void createView() {
        Button viewAppointmentsButton = new Button("View Appointments");
        Button viewPatientHistoryButton = new Button("View Patient History");
        Button manageScheduleButton = new Button("Manage Schedule");
        Button logoutButton = new Button("Logout");

        VBox navBar = new VBox(10, viewAppointmentsButton, viewPatientHistoryButton, manageScheduleButton, logoutButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        Label titleLabel = new Label("Welcome, " + doctorUsername);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        initializeAppointmentTable();
        initializePatientHistoryTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable, patientHistoryTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        view = new BorderPane();
        view.setLeft(navBar);
        view.setCenter(mainContent);

        viewAppointmentsButton.setOnAction(e -> refreshAppointments());
        viewPatientHistoryButton.setOnAction(e -> openPatientHistoryWindow());
        manageScheduleButton.setOnAction(e -> openManageScheduleWindow());
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(new Stage());
            primaryStage.close();
        });
    }

    private void openManageScheduleWindow() {
        Stage manageScheduleStage = new Stage();
        manageScheduleStage.setTitle("Manage Schedule");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label patientLabel = new Label("Patient Username:");
        grid.add(patientLabel, 0, 0);
        TextField patientField = new TextField();
        grid.add(patientField, 1, 0);

        Label dateLabel = new Label("Date (YYYY-MM-DD):");
        grid.add(dateLabel, 0, 1);
        TextField dateField = new TextField();
        grid.add(dateField, 1, 1);

        Label timeLabel = new Label("Time (HH:MM:SS):");
        grid.add(timeLabel, 0, 2);
        TextField timeField = new TextField();
        timeField.setPromptText("e.g., 14:30:00");
        grid.add(timeField, 1, 2);

        Button addButton = new Button("Add Appointment");
        Button updateButton = new Button("Update Appointment");
        Button deleteButton = new Button("Delete Appointment");

        grid.add(addButton, 0, 3);
        grid.add(updateButton, 1, 3);
        grid.add(deleteButton, 2, 3);

        Label statusLabel = new Label();
        grid.add(statusLabel, 0, 4, 3, 1);

        addButton.setOnAction(e -> {
            String patientUsername = patientField.getText();
            String date = dateField.getText();
            String time = timeField.getText();
            if (patientUsername.isEmpty() || date.isEmpty() || time.isEmpty()) {
                statusLabel.setText("Please fill all fields.");
            } else if (!userExists(patientUsername)) {
                statusLabel.setText("Patient username does not exist.");
            } else {
                try {
                    Database.executeUpdate("INSERT INTO appointments (patient_username, doctor_username, date, time) VALUES (?, ?, ?, ?)",
                            patientUsername, doctorUsername, date, time);
                    statusLabel.setText("Appointment added successfully!");
                    refreshAppointments();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Failed to add appointment: " + ex.getMessage());
                }
            }
        });

        updateButton.setOnAction(e -> {
            Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Please select an appointment to update.");
            } else {
                String patientUsername = patientField.getText();
                String date = dateField.getText();
                String time = timeField.getText();
                if (patientUsername.isEmpty() || date.isEmpty() || time.isEmpty()) {
                    statusLabel.setText("Please fill all fields.");
                } else if (!userExists(patientUsername)) {
                    statusLabel.setText("Patient username does not exist.");
                } else {
                    try {
                        Database.executeUpdate("UPDATE appointments SET patient_username = ?, date = ?, time = ? WHERE id = ? AND doctor_username = ?",
                                patientUsername, date, time, selected.getId(), doctorUsername);
                        statusLabel.setText("Appointment updated successfully!");
                        refreshAppointments();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        statusLabel.setText("Failed to update appointment: " + ex.getMessage());
                    }
                }
            }
        });

        deleteButton.setOnAction(e -> {
            Appointment selected = appointmentTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                statusLabel.setText("Please select an appointment to delete.");
            } else {
                try {
                    Database.executeUpdate("DELETE FROM appointments WHERE id = ? AND doctor_username = ?",
                            selected.getId(), doctorUsername);
                    statusLabel.setText("Appointment deleted successfully!");
                    refreshAppointments();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Failed to delete appointment: " + ex.getMessage());
                }
            }
        });

        Scene scene = new Scene(grid, 400, 250);
        manageScheduleStage.setScene(scene);
        manageScheduleStage.show();
    }

    private void openPatientHistoryWindow() {
        Stage historyStage = new Stage();
        historyStage.setTitle("View Patient History");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label patientLabel = new Label("Patient Username:");
        grid.add(patientLabel, 0, 0);
        TextField patientField = new TextField();
        grid.add(patientField, 1, 0);

        Button viewButton = new Button("View History");
        grid.add(viewButton, 1, 1);

        TableView<MedicalRecord> tempHistoryTable = new TableView<>();
        initializePatientHistoryTable(tempHistoryTable);
        grid.add(tempHistoryTable, 0, 2, 2, 1);

        viewButton.setOnAction(e -> {
            String patientUsername = patientField.getText();
            if (patientUsername.isEmpty()) {
                System.out.println("Please enter a patient username.");
            } else if (!userExists(patientUsername)) {
                System.out.println("Patient username does not exist.");
            } else {
                try {
                    ResultSet rs = Database.executeQuery("SELECT * FROM medical_history WHERE patient_username = ?", patientUsername);
                    ObservableList<MedicalRecord> history = FXCollections.observableArrayList();
                    while (rs.next()) {
                        history.add(new MedicalRecord(rs.getInt("id"), rs.getString("patient_username"), rs.getString("record")));
                    }
                    tempHistoryTable.setItems(history);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to load patient history.");
                }
            }
        });

        Scene scene = new Scene(grid, 600, 400);
        historyStage.setScene(scene);
        historyStage.show();
    }

    private void refreshAppointments() {
        try {
            ResultSet rs = Database.executeQuery("SELECT * FROM appointments WHERE doctor_username = ?", doctorUsername);
            ObservableList<Appointment> appointments = FXCollections.observableArrayList();
            while (rs.next()) {
                appointments.add(new Appointment(rs.getInt("id"), rs.getString("patient_username"), rs.getString("doctor_username"), rs.getString("date"), rs.getString("time")));
            }
            appointmentTable.setItems(appointments);
            patientHistoryTable.setVisible(false);
            appointmentTable.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to load appointments.");
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

    private void initializePatientHistoryTable() {
        initializePatientHistoryTable(patientHistoryTable);
    }

    private void initializePatientHistoryTable(TableView<MedicalRecord> table) {
        TableColumn<MedicalRecord, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MedicalRecord, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<MedicalRecord, String> recordColumn = new TableColumn<>("Record");
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));

        table.getColumns().setAll(idColumn, patientColumn, recordColumn);
        table.setVisible(false);
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

    public static class MedicalRecord {
        private final int id;
        private final String patientUsername;
        private final String record;

        public MedicalRecord(int id, String patientUsername, String record) {
            this.id = id;
            this.patientUsername = patientUsername;
            this.record = record;
        }

        public int getId() { return id; }
        public String getPatientUsername() { return patientUsername; }
        public String getRecord() { return record; }
    }
}