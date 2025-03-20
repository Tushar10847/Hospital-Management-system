package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminPage {

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Page");

        // Navigation Bar
        Button manageUsersButton = new Button("Manage Users");
        Button manageDoctorsButton = new Button("Manage Doctors");
        Button viewReportsButton = new Button("View Reports");

        VBox navBar = new VBox(10, manageUsersButton, manageDoctorsButton, viewReportsButton);
        navBar.setStyle("-fx-background-color: #333; -fx-padding: 20;");
        navBar.setPrefWidth(150);

        // Main Content
        Label welcomeLabel = new Label("Welcome, Admin!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #2c3e50;");

        // TableView for displaying data
        TableView<User> userTable = new TableView<>();
        TableView<Doctor> doctorTable = new TableView<>();
        TableView<Report> reportTable = new TableView<>();

        VBox mainContent = new VBox(20, welcomeLabel, userTable, doctorTable, reportTable);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setStyle("-fx-padding: 20;");

        // Root Layout
        BorderPane root = new BorderPane();
        root.setLeft(navBar);
        root.setCenter(mainContent);

        // Button Actions
        manageUsersButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM users WHERE role = 'Patient' OR role = 'Receptionist'");
                ObservableList<User> users = FXCollections.observableArrayList();
                while (rs.next()) {
                    users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("name"),
                        rs.getString("email")
                    ));
                }
                initializeUserTable(userTable);
                userTable.setItems(users);
                doctorTable.setVisible(false);
                reportTable.setVisible(false);
                userTable.setVisible(true);
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
                initializeDoctorTable(doctorTable);
                doctorTable.setItems(doctors);
                userTable.setVisible(false);
                reportTable.setVisible(false);
                doctorTable.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        viewReportsButton.setOnAction(e -> {
            try {
                ResultSet rs = Database.executeQuery("SELECT * FROM medical_history");
                ObservableList<Report> reports = FXCollections.observableArrayList();
                while (rs.next()) {
                    reports.add(new Report(
                        rs.getInt("id"),
                        rs.getString("patient_username"),
                        rs.getString("record")
                    ));
                }
                initializeReportTable(reportTable);
                reportTable.setItems(reports);
                userTable.setVisible(false);
                doctorTable.setVisible(false);
                reportTable.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Scene
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Initialize the User Table.
     */
    private void initializeUserTable(TableView<User> userTable) {
        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        TableColumn<User, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        userTable.getColumns().addAll(idColumn, usernameColumn, roleColumn, nameColumn, emailColumn);
        userTable.setVisible(false); // Initially hidden
    }

    /**
     * Initialize the Doctor Table.
     */
    private void initializeDoctorTable(TableView<Doctor> doctorTable) {
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

    /**
     * Initialize the Report Table.
     */
    private void initializeReportTable(TableView<Report> reportTable) {
        TableColumn<Report, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Report, String> patientColumn = new TableColumn<>("Patient");
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));

        TableColumn<Report, String> recordColumn = new TableColumn<>("Record");
        recordColumn.setCellValueFactory(new PropertyValueFactory<>("record"));

        reportTable.getColumns().addAll(idColumn, patientColumn, recordColumn);
        reportTable.setVisible(false); // Initially hidden
    }

    /**
     * Model class for Users.
     */
    public static class User {
        private final int id;
        private final String username;
        private final String role;
        private final String name;
        private final String email;

        public User(int id, String username, String role, String name, String email) {
            this.id = id;
            this.username = username;
            this.role = role;
            this.name = name;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
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

    /**
     * Model class for Reports.
     */
    public static class Report {
        private final int id;
        private final String patientUsername;
        private final String record;

        public Report(int id, String patientUsername, String record) {
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