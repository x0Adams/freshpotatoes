package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.Movie;
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
class MovieDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private MovieDaoImpl movieDao;

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
        when(resultSet.getString("name")).thenReturn("The Matrix");
        when(resultSet.getString("poster_path")).thenReturn("/matrix.jpg");
        when(resultSet.getInt("duration")).thenReturn(136);
        when(resultSet.getDate("release_date")).thenReturn(Date.valueOf("1999-03-31"));
        when(resultSet.getString("youtube_movie")).thenReturn("yt_link");
        when(resultSet.getString("google_knowledge_graph")).thenReturn("kg_link");
        when(resultSet.getString("trailer")).thenReturn("trailer_link");

        Optional<Movie> result = movieDao.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("The Matrix", result.get().getName());
        assertEquals(LocalDate.of(1999, 3, 31), result.get().getReleaseDate());
        verify(preparedStatement).setInt(1, 1);
    }

    @Test
    void findById_NotFound() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Movie> result = movieDao.findById(99);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("The Matrix");
        when(resultSet.getString("poster_path")).thenReturn("/matrix.jpg");
        when(resultSet.getInt("duration")).thenReturn(136);
        when(resultSet.getDate("release_date")).thenReturn(Date.valueOf("1999-03-31"));
        when(resultSet.getString("youtube_movie")).thenReturn("yt_link");
        when(resultSet.getString("google_knowledge_graph")).thenReturn("kg_link");
        when(resultSet.getString("trailer")).thenReturn("trailer_link");

        List<Movie> results = movieDao.findAll();

        assertEquals(1, results.size());
        assertEquals(1, results.get(0).getId());
        assertEquals("The Matrix", results.get(0).getName());
    }

    @Test
    void save() throws SQLException {
        Movie movie = new Movie();
        movie.setName("Inception");
        movie.setPosterPath("/inception.jpg");
        movie.setDuration(148);
        movie.setReleaseDate(LocalDate.of(2010, 7, 16));
        movie.setYoutubeMovie("yt_link");
        movie.setGoogleKnowledgeGraph("kg_link");
        movie.setTrailer("trailer_link");

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        Movie savedMovie = movieDao.save(movie);

        assertEquals(10, savedMovie.getId());
        assertEquals("Inception", savedMovie.getName());
        verify(preparedStatement).setString(1, "Inception");
        verify(preparedStatement).setDate(4, Date.valueOf(LocalDate.of(2010, 7, 16)));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        Movie movie = new Movie();
        movie.setId(1);
        movie.setName("Interstellar");
        movie.setPosterPath("/interstellar.jpg");
        movie.setDuration(169);
        movie.setReleaseDate(LocalDate.of(2014, 11, 7));
        movie.setYoutubeMovie("yt_link");
        movie.setGoogleKnowledgeGraph("kg_link");
        movie.setTrailer("trailer_link");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        movieDao.update(movie);

        verify(preparedStatement).setString(1, "Interstellar");
        verify(preparedStatement).setInt(8, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        movieDao.delete(1);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).executeUpdate();
    }
}