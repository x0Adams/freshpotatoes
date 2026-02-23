package hu.freshpotatoes.controller;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DatabaseConnection {

    // The single pool instance for your whole application
    private static HikariDataSource dataSource;

    // Static block runs exactly once when this class is first loaded into memory
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mariadb://localhost:3306/fresh_potatoes");
            config.setUsername("root");
            config.setPassword("");
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            // Pool configuration settings
            config.setMaximumPoolSize(10); // Don't create too many connections
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30 seconds

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    // Private constructor prevents anyone from instantiating this class via 'new DatabaseConnection()'
    private DatabaseConnection() {
    }

    // Instead of getConnection(), we return the DataSource itself!
    public static DataSource getDataSource() {
        return dataSource;
    }
}