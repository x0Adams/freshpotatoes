package hu.freshpotatoes;

import hu.freshpotatoes.dao.DatabaseConnection;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection();

        if (databaseConnection.getConnection() != null) {
            System.out.println("Successfully connected to MariaDB!");
            System.out.println("Driver Name: " + databaseConnection.getConnection().getMetaData().getDriverName());
        }
    }
}