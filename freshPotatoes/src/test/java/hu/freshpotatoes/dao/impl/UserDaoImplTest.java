package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private UserDaoImpl userDao;

    private Instant now;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        now = Instant.now();
    }

    @Test
    void findById() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("emai")).thenReturn("test@example.com");
        when(resultSet.getString("name")).thenReturn("John Doe");
        when(resultSet.getString("password_hash")).thenReturn("hashed_password");
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.from(now));

        Optional<User> result = userDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("test@example.com", result.get().getEmai());
        assertEquals("John Doe", result.get().getName());
        assertEquals("hashed_password", result.get().getPasswordHash());
        assertEquals(now, result.get().getCreationDate());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_NotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<User> result = userDao.findById(99);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("emai")).thenReturn("test@example.com");
        when(resultSet.getString("name")).thenReturn("John Doe");
        when(resultSet.getString("password_hash")).thenReturn("hashed_password");
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.from(now));

        List<User> results = userDao.findAll();

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals("test@example.com", results.get(0).getEmai());
        assertEquals("John Doe", results.get(0).getName());
    }

    @Test
    void save() throws SQLException {
        User user = new User();
        user.setEmai("newuser@example.com");
        user.setName("Jane Doe");
        user.setPasswordHash("secret_hash");
        user.setCreationDate(now);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        User savedUser = userDao.save(user);

        assertEquals(5, savedUser.getId());
        verify(preparedStatement).setString(1, "newuser@example.com");
        verify(preparedStatement).setString(2, "Jane Doe");
        verify(preparedStatement).setString(3, "secret_hash");
        verify(preparedStatement).setTimestamp(4, Timestamp.from(now));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        User user = new User();
        user.setId(1);
        user.setEmai("updated@example.com");
        user.setName("Updated Name");
        user.setPasswordHash("new_hash");
        user.setCreationDate(now);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        userDao.update(user);

        verify(preparedStatement).setString(1, "updated@example.com");
        verify(preparedStatement).setString(2, "Updated Name");
        verify(preparedStatement).setString(3, "new_hash");
        verify(preparedStatement).setTimestamp(4, Timestamp.from(now));
        verify(preparedStatement).setInt(5, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        userDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }
}