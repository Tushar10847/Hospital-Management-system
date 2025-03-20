package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "root";
    private static final String PASSWORD = "thapatushar11"; // Replace with your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    public static int executeUpdate(String query, Object... params) throws SQLException {
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeUpdate();
    }
}