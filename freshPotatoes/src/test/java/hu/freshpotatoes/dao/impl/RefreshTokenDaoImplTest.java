package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.RefreshToken;
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
class RefreshTokenDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private RefreshTokenDaoImpl refreshTokenDao;

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
        when(resultSet.getInt("user_id")).thenReturn(5);
        when(resultSet.getString("token")).thenReturn("sample_token_xyz");
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.from(now));
        when(resultSet.getTimestamp("expiration_date")).thenReturn(Timestamp.from(now.plusSeconds(3600)));
        when(resultSet.getInt("used")).thenReturn(0);

        Optional<RefreshToken> result = refreshTokenDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals(5, result.get().getUser().getId());
        assertEquals("sample_token_xyz", result.get().getToken());
        assertEquals(now, result.get().getCreationDate());
        assertEquals(now.plusSeconds(3600), result.get().getExpirationDate());
        assertEquals(0, result.get().getUsed());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_NotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<RefreshToken> result = refreshTokenDao.findById(99);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getInt("user_id")).thenReturn(5);
        when(resultSet.getString("token")).thenReturn("sample_token_xyz");
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.from(now));
        when(resultSet.getTimestamp("expiration_date")).thenReturn(Timestamp.from(now.plusSeconds(3600)));
        when(resultSet.getInt("used")).thenReturn(0);

        List<RefreshToken> results = refreshTokenDao.findAll();

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals("sample_token_xyz", results.get(0).getToken());
    }

    @Test
    void save() throws SQLException {
        User user = new User();
        user.setId(5);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken("new_token_123");
        token.setCreationDate(now);
        token.setExpirationDate(now.plusSeconds(3600));
        token.setUsed(0);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        RefreshToken savedToken = refreshTokenDao.save(token);

        assertEquals(10, savedToken.getId());
        verify(preparedStatement).setInt(1, 5);
        verify(preparedStatement).setString(2, "new_token_123");
        verify(preparedStatement).setTimestamp(3, Timestamp.from(now));
        verify(preparedStatement).setTimestamp(4, Timestamp.from(now.plusSeconds(3600)));
        verify(preparedStatement).setInt(5, 0);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        User user = new User();
        user.setId(5);

        RefreshToken token = new RefreshToken();
        token.setId(1);
        token.setUser(user);
        token.setToken("updated_token_456");
        token.setCreationDate(now);
        token.setExpirationDate(now.plusSeconds(3600));
        token.setUsed(1);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        refreshTokenDao.update(token);

        verify(preparedStatement).setInt(1, 5);
        verify(preparedStatement).setString(2, "updated_token_456");
        verify(preparedStatement).setTimestamp(3, Timestamp.from(now));
        verify(preparedStatement).setTimestamp(4, Timestamp.from(now.plusSeconds(3600)));
        verify(preparedStatement).setInt(5, 1);
        verify(preparedStatement).setInt(6, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        refreshTokenDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }
}