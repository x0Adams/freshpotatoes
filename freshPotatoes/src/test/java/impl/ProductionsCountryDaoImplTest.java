package impl;

import hu.freshpotatoes.dao.impl.ProductionsCountryDaoImpl;
import hu.freshpotatoes.model.ProductionsCountry;
import hu.freshpotatoes.model.ProductionsCountryId;
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
class ProductionsCountryDaoImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private ProductionsCountryDaoImpl productionsCountryDao;

    @BeforeEach
    void setUp() throws SQLException {
        lenient().when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void findById() throws SQLException {
        ProductionsCountryId id = new ProductionsCountryId();
        id.setMovie(1);
        id.setCountry(5);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("movie")).thenReturn(1);
        when(resultSet.getInt("country")).thenReturn(5);

        Optional<ProductionsCountry> result = productionsCountryDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId().getMovie());
        assertEquals(5, result.get().getId().getCountry());
        assertEquals(1, result.get().getMovie().getId());
        assertEquals(5, result.get().getCountry().getId());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 5);
    }

    @Test
    void findById_NotFound() throws SQLException {
        ProductionsCountryId id = new ProductionsCountryId();
        id.setMovie(99);
        id.setCountry(99);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<ProductionsCountry> result = productionsCountryDao.findById(id);

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
        when(resultSet.getInt("country")).thenReturn(5, 7);

        List<ProductionsCountry> results = productionsCountryDao.findAll();

        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getId().getMovie());
        assertEquals(5, results.get(0).getId().getCountry());
        assertEquals(3, results.get(1).getId().getMovie());
        assertEquals(7, results.get(1).getId().getCountry());
    }

    @Test
    void save() throws SQLException {
        ProductionsCountryId id = new ProductionsCountryId();
        id.setMovie(1);
        id.setCountry(5);
        ProductionsCountry productionsCountry = new ProductionsCountry();
        productionsCountry.setId(id);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        ProductionsCountry savedEntity = productionsCountryDao.save(productionsCountry);

        assertEquals(1, savedEntity.getId().getMovie());
        assertEquals(5, savedEntity.getId().getCountry());
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 5);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void update() {
        ProductionsCountry productionsCountry = new ProductionsCountry();
        assertDoesNotThrow(() -> productionsCountryDao.update(productionsCountry));
    }

    @Test
    void delete() throws SQLException {
        ProductionsCountryId id = new ProductionsCountryId();
        id.setMovie(1);
        id.setCountry(5);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        productionsCountryDao.delete(id);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 5);
        verify(preparedStatement).executeUpdate();
    }
}