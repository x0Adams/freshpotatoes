package impl;

import hu.freshpotatoes.dao.impl.GenreDaoImpl;
import hu.freshpotatoes.model.Genre;
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
class GenreDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private GenreDaoImpl genreDao;

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
        when(resultSet.getString("name")).thenReturn("Action");

        Optional<Genre> result = genreDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Action", result.get().getName());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_NotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Genre> result = genreDao.findById(99);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(1, 2);
        when(resultSet.getString("name")).thenReturn("Action", "Comedy");

        List<Genre> results = genreDao.findAll();

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals("Action", results.get(0).getName());
        assertEquals(2, results.get(1).getId());
        assertEquals("Comedy", results.get(1).getName());
    }

    @Test
    void save() throws SQLException {
        Genre genre = new Genre();
        genre.setName("Sci-Fi");

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        Genre savedGenre = genreDao.save(genre);

        assertEquals(10, savedGenre.getId());
        assertEquals("Sci-Fi", savedGenre.getName());
        verify(preparedStatement).setString(1, "Sci-Fi");
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Horror");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        genreDao.update(genre);

        verify(preparedStatement).setString(1, "Horror");
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        genreDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }
}