package hu.freshpotatoes;

import hu.freshpotatoes.controller.DatabaseConnection;
import hu.freshpotatoes.dao.MovieDao;
import hu.freshpotatoes.dao.impl.MovieDaoImpl;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
//        DatabaseConnection databaseConnection = new DatabaseConnection();
//
//        if (databaseConnection.getConnection() != null) {
//            System.out.println("Successfully connected to MariaDB!");
//            System.out.println("Driver Name: " + databaseConnection.getConnection().getMetaData().getDriverName());
//        }
//        databaseConnection.getConnection().close();


        javax.sql.DataSource dataSource = DatabaseConnection.getDataSource();

        MovieDao movieDao = new MovieDaoImpl(dataSource);

        movieDao.findAll().forEach(movie -> {
            System.out.println(movie.getName());
        });

    }
}