package impl;

import hu.freshpotatoes.dao.impl.RoleDaoImpl;
import hu.freshpotatoes.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private RoleDaoImpl roleDao;

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
        when(resultSet.getString("role")).thenReturn("ADMIN");

        Optional<Role> result = roleDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("ADMIN", result.get().getRole());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_NotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Role> result = roleDao.findById(99);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("role")).thenReturn("ADMIN", "USER");

        List<Role> results = roleDao.findAll();

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals("ADMIN", results.get(0).getRole());
        assertEquals(2, results.get(1).getId());
        assertEquals("USER", results.get(1).getRole());
    }

    @Test
    void save() throws SQLException {
        Role role = new Role();
        role.setRole("MODERATOR");

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(3);

        Role savedRole = roleDao.save(role);

        assertEquals(3, savedRole.getId());
        assertEquals("MODERATOR", savedRole.getRole());
        verify(preparedStatement).setString(1, "MODERATOR");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        Role role = new Role();
        role.setId(1);
        role.setRole("SUPER_ADMIN");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        roleDao.update(role);

        verify(preparedStatement).setString(1, "SUPER_ADMIN");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        roleDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }
}