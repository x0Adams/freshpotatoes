package hu.freshpotatoes.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public DatabaseConnection() {
    }

    public Connection getConnection() {
        String url = "jdbc:mariadb://localhost:3306/fresh_potatoes";
        String user = "root";
        String password = "";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            return connection;
        } catch (SQLException e) {
            // throw new RuntimeException(e);
            System.out.println("Connection exception: " + e);
        }
        return null;
    }
}
