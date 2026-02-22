package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.Country;
import hu.freshpotatoes.model.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private StaffDaoImpl staffDao;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void findById() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Steven Spielberg");
        when(resultSet.getDate("birthday")).thenReturn(Date.valueOf("1946-12-18"));
        when(resultSet.getInt("birth_country")).thenReturn(10);

        Optional<Staff> result = staffDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Steven Spielberg", result.get().getName());
        assertEquals(LocalDate.of(1946, 12, 18), result.get().getBirthday());
        assertEquals(10, result.get().getBirthCountry().getId());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_NotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Staff> result = staffDao.findById(99);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Steven Spielberg");
        when(resultSet.getDate("birthday")).thenReturn(Date.valueOf("1946-12-18"));
        when(resultSet.getInt("birth_country")).thenReturn(10);

        List<Staff> results = staffDao.findAll();

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals("Steven Spielberg", results.get(0).getName());
        assertEquals(LocalDate.of(1946, 12, 18), results.get(0).getBirthday());
    }

    @Test
    void save() throws SQLException {
        Country country = new Country();
        country.setId(10);

        Staff staff = new Staff();
        staff.setName("Christopher Nolan");
        staff.setBirthday(LocalDate.of(1970, 7, 30));
        staff.setBirthCountry(country);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(2);

        Staff savedStaff = staffDao.save(staff);

        assertEquals(2, savedStaff.getId());
        assertEquals("Christopher Nolan", savedStaff.getName());
        verify(preparedStatement).setString(1, "Christopher Nolan");
        verify(preparedStatement).setDate(2, Date.valueOf(LocalDate.of(1970, 7, 30)));
        verify(preparedStatement).setInt(3, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        Country country = new Country();
        country.setId(10);

        Staff staff = new Staff();
        staff.setId(1);
        staff.setName("Quentin Tarantino");
        staff.setBirthday(LocalDate.of(1963, 3, 27));
        staff.setBirthCountry(country);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        staffDao.update(staff);

        verify(preparedStatement).setString(1, "Quentin Tarantino");
        verify(preparedStatement).setDate(2, Date.valueOf(LocalDate.of(1963, 3, 27)));
        verify(preparedStatement).setInt(3, 10);
        verify(preparedStatement).setInt(4, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        staffDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }
}