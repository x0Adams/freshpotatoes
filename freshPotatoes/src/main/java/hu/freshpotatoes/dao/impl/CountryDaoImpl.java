package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.CountryDao;
import hu.freshpotatoes.model.Country;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

public class CountryDaoImpl implements CountryDao {
    private DataSource dataSource;

    public CountryDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Country mapRow(ResultSet resultSet) throws SQLException {
        return new Country(
                resultSet.getInt("id"),
                resultSet.getString("name")
        );
    }

    @Override
    public String findByName(String countryName) {
        String sql = "SELECT * FROM country WHERE name = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, countryName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return String.valueOf(resultSet);
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return "";
    }

    @Override
    public Optional<Country> findById(Integer id) {
        String sql = "SELECT * FROM country WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            // throw new RuntimeException(e);
            System.out.println(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Country> findAll() {
        return List.of();
    }

    @Override
    public Country save(Country entity) {
        String sql = "INSERT INTO country (name) VALUES (?)";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, entity.getName());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return entity;
    }

    @Override
    public void update(Country entity) {
        String sql = "UPDATE country SET name = ? WHERE id = ?";

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, entity.getName());
            statement.setInt(2, entity.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM country WHERE id = ?";
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            statement.executeQuery();
        } catch (SQLException e) {
            // throw new RuntimeException(e);
            System.out.println(e);
        }
    }
}
