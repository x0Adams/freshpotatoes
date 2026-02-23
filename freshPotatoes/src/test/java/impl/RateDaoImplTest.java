package impl;

import hu.freshpotatoes.dao.impl.RateDaoImpl;
import hu.freshpotatoes.model.Rate;
import hu.freshpotatoes.model.RateId;
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
class RateDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private RateDaoImpl rateDao;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void findById() throws SQLException {
        RateId id = new RateId();
        id.setUser(1);
        id.setMovie(2);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("user")).thenReturn(1);
        when(resultSet.getInt("movie")).thenReturn(2);
        when(resultSet.getByte("rating")).thenReturn((byte) 5);

        Optional<Rate> result = rateDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId().getUser());
        assertEquals(2, result.get().getId().getMovie());
        assertEquals((byte) 5, result.get().getRating());
        assertEquals(1, result.get().getUser().getId());
        assertEquals(2, result.get().getMovie().getId());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
    }

    @Test
    void findById_NotFound() throws SQLException {
        RateId id = new RateId();
        id.setUser(99);
        id.setMovie(99);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<Rate> result = rateDao.findById(id);

        assertFalse(result.isPresent());
        verify(preparedStatement).setInt(1, 99);
        verify(preparedStatement).setInt(2, 99);
    }

    @Test
    void findAll() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("user")).thenReturn(1, 3);
        when(resultSet.getInt("movie")).thenReturn(2, 4);
        when(resultSet.getByte("rating")).thenReturn((byte) 4, (byte) 5);

        List<Rate> results = rateDao.findAll();

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId().getUser());
        assertEquals(2, results.get(0).getId().getMovie());
        assertEquals((byte) 4, results.get(0).getRating());
        assertEquals(3, results.get(1).getId().getUser());
        assertEquals(4, results.get(1).getId().getMovie());
        assertEquals((byte) 5, results.get(1).getRating());
    }

    @Test
    void save() throws SQLException {
        RateId id = new RateId();
        id.setUser(1);
        id.setMovie(2);
        Rate rate = new Rate();
        rate.setId(id);
        rate.setRating((byte) 5);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        Rate savedRate = rateDao.save(rate);

        assertEquals(1, savedRate.getId().getUser());
        assertEquals(2, savedRate.getId().getMovie());
        assertEquals((byte) 5, savedRate.getRating());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setByte(3, (byte) 5);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() throws SQLException {
        RateId id = new RateId();
        id.setUser(1);
        id.setMovie(2);
        Rate rate = new Rate();
        rate.setId(id);
        rate.setRating((byte) 3);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        rateDao.update(rate);

        verify(preparedStatement).setByte(1, (byte) 3);
        verify(preparedStatement).setInt(2, 1);
        verify(preparedStatement).setInt(3, 2);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void delete() throws SQLException {
        RateId id = new RateId();
        id.setUser(1);
        id.setMovie(2);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        rateDao.delete(id);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).executeUpdate();
    }
}