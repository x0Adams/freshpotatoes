package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.GenreMovie;
import hu.freshpotatoes.model.GenreMovieId;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreMovieDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private GenreMovieDaoImpl genreMovieDao;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void findById() throws SQLException {
        GenreMovieId id = new GenreMovieId();
        id.setMovie(1);
        id.setGenre(2);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("movie")).thenReturn(1);
        when(resultSet.getInt("genre")).thenReturn(2);

        Optional<GenreMovie> result = genreMovieDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId().getMovie());
        assertEquals(2, result.get().getId().getGenre());
        assertEquals(1, result.get().getMovie().getId());
        assertEquals(2, result.get().getGenre().getId());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
    }

    @Test
    void findById_NotFound() throws SQLException {
        GenreMovieId id = new GenreMovieId();
        id.setMovie(99);
        id.setGenre(99);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<GenreMovie> result = genreMovieDao.findById(id);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
        verify(preparedStatement).setInt(2, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("movie")).thenReturn(1, 3);
        when(resultSet.getInt("genre")).thenReturn(2, 4);

        List<GenreMovie> results = genreMovieDao.findAll();

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId().getMovie());
        assertEquals(2, results.get(0).getId().getGenre());
        assertEquals(3, results.get(1).getId().getMovie());
        assertEquals(4, results.get(1).getId().getGenre());
    }

    @Test
    void save() throws SQLException {
        GenreMovieId id = new GenreMovieId();
        id.setMovie(1);
        id.setGenre(2);
        GenreMovie genreMovie = new GenreMovie();
        genreMovie.setId(id);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        GenreMovie savedGenreMovie = genreMovieDao.save(genreMovie);

        assertEquals(1, savedGenreMovie.getId().getMovie());
        assertEquals(2, savedGenreMovie.getId().getGenre());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() {
        GenreMovie genreMovie = new GenreMovie();
        assertDoesNotThrow(() -> genreMovieDao.update(genreMovie));
    }

    @Test
    void delete() throws SQLException {
        GenreMovieId id = new GenreMovieId();
        id.setMovie(1);
        id.setGenre(2);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        genreMovieDao.delete(id);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).executeUpdate();
    }
}