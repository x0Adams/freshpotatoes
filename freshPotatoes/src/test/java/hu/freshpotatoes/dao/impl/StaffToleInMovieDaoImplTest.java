package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.model.StaffToleInMovie;
import hu.freshpotatoes.model.StaffToleInMovieId;
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
class StaffToleInMovieDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private StaffToleInMovieDaoImpl staffToleInMovieDao;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void findById() throws SQLException {
        StaffToleInMovieId id = new StaffToleInMovieId();
        id.setRole(1);
        id.setMovie(2);
        id.setStaff(3);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("role")).thenReturn(1);
        when(resultSet.getInt("movie")).thenReturn(2);
        when(resultSet.getInt("staff")).thenReturn(3);

        Optional<StaffToleInMovie> result = staffToleInMovieDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId().getRole());
        assertEquals(2, result.get().getId().getMovie());
        assertEquals(3, result.get().getId().getStaff());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setInt(3, 3);
    }

    @Test
    void findById_NotFound() throws SQLException {
        StaffToleInMovieId id = new StaffToleInMovieId();
        id.setRole(99);
        id.setMovie(99);
        id.setStaff(99);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<StaffToleInMovie> result = staffToleInMovieDao.findById(id);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
        verify(preparedStatement).setInt(2, 99);
        verify(preparedStatement).setInt(3, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("role")).thenReturn(1, 4);
        when(resultSet.getInt("movie")).thenReturn(2, 5);
        when(resultSet.getInt("staff")).thenReturn(3, 6);

        List<StaffToleInMovie> results = staffToleInMovieDao.findAll();

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId().getRole());
        assertEquals(2, results.get(0).getId().getMovie());
        assertEquals(3, results.get(0).getId().getStaff());
        assertEquals(4, results.get(1).getId().getRole());
        assertEquals(5, results.get(1).getId().getMovie());
        assertEquals(6, results.get(1).getId().getStaff());
    }

    @Test
    void save() throws SQLException {
        StaffToleInMovieId id = new StaffToleInMovieId();
        id.setRole(1);
        id.setMovie(2);
        id.setStaff(3);
        StaffToleInMovie entity = new StaffToleInMovie();
        entity.setId(id);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        StaffToleInMovie savedEntity = staffToleInMovieDao.save(entity);

        assertEquals(1, savedEntity.getId().getRole());
        assertEquals(2, savedEntity.getId().getMovie());
        assertEquals(3, savedEntity.getId().getStaff());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() {
        StaffToleInMovie entity = new StaffToleInMovie();
        assertDoesNotThrow(() -> staffToleInMovieDao.update(entity));
    }

    @Test
    void delete() throws SQLException {
        StaffToleInMovieId id = new StaffToleInMovieId();
        id.setRole(1);
        id.setMovie(2);
        id.setStaff(3);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        staffToleInMovieDao.delete(id);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).executeUpdate();
    }
}