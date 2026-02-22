package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.Country;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private CountryDaoImpl countryDao;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void findById_ShouldReturnCountry_WhenCountryExists() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Hungary");

        Optional<Country> result = countryDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Hungary", result.get().getName());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_ShouldReturnEmptyOptional_WhenCountryDoesNotExist() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Country> result = countryDao.findById(99);

        assertTrue(result.isEmpty());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findByName_ShouldReturnString_WhenCountryExists() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        String expectedString = String.valueOf(resultSet);

        String result = countryDao.findByName("Hungary");

        assertEquals(expectedString, result);
        verify(preparedStatement).setString(1, "Hungary");
    }

    @Test
    void findAll_ShouldReturnEmptyList() {
        List<Country> result = countryDao.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldInsertAndSetGeneratedId() throws SQLException {
        Country newCountry = new Country(0, "Austria");

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        Country savedCountry = countryDao.save(newCountry);

        assertEquals(5, savedCountry.getId());
        assertEquals("Austria", savedCountry.getName());
        verify(preparedStatement).setString(1, "Austria");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update_ShouldExecuteUpdate() throws SQLException {
        Country existingCountry = new Country(1, "Slovakia");
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        countryDao.update(existingCountry);

        verify(preparedStatement).setString(1, "Slovakia");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete_ShouldExecuteQuery() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        countryDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeQuery();
    }
}