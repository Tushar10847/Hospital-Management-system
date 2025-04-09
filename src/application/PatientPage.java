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

public class PatientPage {

    private BorderPane view;
    private Stage primaryStage;
    private String patientUsername;
    private TableView<Appointment> appointmentTable = new TableView<>();
    private TableView<MedicalRecord> historyTable = new TableView<>();

    public PatientPage(Stage primaryStage, String patientUsername) {
        this.primaryStage = primaryStage;
        this.patientUsername = patientUsername;
        createView();
    }

    private void createView() {
        Button bookAppointmentButton = new Button("Book Appointment");
        Button viewAppointmentsButton = new Button("View Appointments");
        Button viewHistoryButton = new Button("View History");
        Button logoutButton = new Button("Logout");

        VBox navBar = new VBox(10, bookAppointmentButton, viewAppointmentsButton, viewHistoryButton, logoutButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        Label titleLabel = new Label("Welcome, " + patientUsername);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        initializeAppointmentTable();
        initializeHistoryTable();

        VBox mainContent = new VBox(20, titleLabel, appointmentTable, historyTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        view = new BorderPane();
        view.setLeft(navBar);
        view.setCenter(mainContent);

        bookAppointmentButton.setOnAction(e -> openBookAppointmentWindow());
        viewAppointmentsButton.setOnAction(e -> refreshAppointments());
        viewHistoryButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM medical_history WHERE patient_username = ?", patientUsername);
                ObservableList<MedicalRecord> history = FXCollections.observableArrayList();
                while (rs.next()) {
                    history.add(new MedicalRecord(rs.getInt("id"), rs.getString("patient_username"), rs.getString("record")));
                }
                historyTable.setItems(history);
                appointmentTable.setVisible(false);
                historyTable.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        logoutButton.setOnAction(e -> {
            LoginPage loginPage = new LoginPage();
            loginPage.start(new Stage());
            primaryStage.close();
        });
    }

    private void openBookAppointmentWindow() {
        Stage bookStage = new Stage();
        bookStage.setTitle("Book Appointment");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label doctorLabel = new Label("Doctor Username:");
        grid.add(doctorLabel, 0, 0);
        TextField doctorField = new TextField();
        grid.add(doctorField, 1, 0);

        Label dateLabel = new Label("Date (YYYY-MM-DD):");
        grid.add(dateLabel, 0, 1);
        TextField dateField = new TextField();
        grid.add(dateField, 1, 1);

        Label timeLabel = new Label("Time (HH:MM:SS):");
        grid.add(timeLabel, 0, 2);
        TextField timeField = new TextField();
        timeField.setPromptText("e.g., 14:30:00");
        grid.add(timeField, 1, 2);

        Button bookButton = new Button("Book");
        grid.add(bookButton, 1, 3);

        bookButton.setOnAction(e -> {
            String doctorUsername = doctorField.getText();
            String date = dateField.getText();
            String time = timeField.getText();
            if (doctorUsername.isEmpty() || date.isEmpty() || time.isEmpty()) {
                System.out.println("Please fill all fields.");
            } else {
                try {
                    Database.executeUpdate("INSERT INTO appointments (patient_username, doctor_username, date, time) VALUES (?, ?, ?, ?)",
                            patientUsername, doctorUsername, date, time);
                    System.out.println("Appointment booked successfully!");
                    bookStage.close();
                    refreshAppointments();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.out.println("Failed to book appointment.");
                }
            }
        });

        Scene scene = new Scene(grid, 400, 200);
        bookStage.setScene(scene);
        bookStage.show();
    }

    private void refreshAppointments() {
        try {
            ResultSet rs = Database.executeQuery("SELECT * FROM appointments WHERE patient_username = ?", patientUsername);
            ObservableList<Appointment> appointments = FXCollections.observableArrayList();
            while (rs.next()) {
                appointments.add(new Appointment(rs.getInt("id"), rs.getString("patient_username"), rs.getString("doctor_username"), rs.getString("date"), rs.getString("time")));
            }
            appointmentTable.setItems(appointments);
            historyTable.setVisible(false);
            appointmentTable.setVisible(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
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

    private void initializeHistoryTable() {
        TableColumn<MedicalRecord, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MedicalRecord, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<MedicalRecord, String> recordColumn = new TableColumn<>("Record");
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));

        historyTable.getColumns().setAll(idColumn, patientColumn, recordColumn);
        historyTable.setVisible(false);
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