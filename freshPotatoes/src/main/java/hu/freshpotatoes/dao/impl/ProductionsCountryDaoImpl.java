package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.ProductionsCountryDao;
import hu.freshpotatoes.model.Country;
import hu.freshpotatoes.model.Movie;
import hu.freshpotatoes.model.ProductionsCountry;
import hu.freshpotatoes.model.ProductionsCountryId;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductionsCountryDaoImpl implements ProductionsCountryDao {

    private final DataSource dataSource;

    public ProductionsCountryDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<ProductionsCountry> findById(ProductionsCountryId id) {
        String sql = "SELECT movie, country FROM fresh_potatoes.productions_country WHERE movie = ? AND country = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getMovie());
            stmt.setInt(2, id.getCountry());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProductionsCountry(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProductionsCountry> findAll() {
        String sql = "SELECT movie, country FROM fresh_potatoes.productions_country";
        List<ProductionsCountry> productionsCountries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                productionsCountries.add(mapRowToProductionsCountry(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productionsCountries;
    }

    @Override
    public ProductionsCountry save(ProductionsCountry entity) {
        String sql = "INSERT INTO fresh_potatoes.productions_country (movie, country) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId().getMovie());
            stmt.setInt(2, entity.getId().getCountry());
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ProductionsCountry entity) {
    }

    @Override
    public void delete(ProductionsCountryId id) {
        String sql = "DELETE FROM fresh_potatoes.productions_country WHERE movie = ? AND country = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getMovie());
            stmt.setInt(2, id.getCountry());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ProductionsCountry mapRowToProductionsCountry(ResultSet rs) throws SQLException {
        ProductionsCountry productionsCountry = new ProductionsCountry();
        ProductionsCountryId id = new ProductionsCountryId();
        id.setMovie(rs.getInt("movie"));
        id.setCountry(rs.getInt("country"));
        productionsCountry.setId(id);

        Movie movie = new Movie();
        movie.setId(rs.getInt("movie"));
        productionsCountry.setMovie(movie);

        Country country = new Country();
        country.setId(rs.getInt("country"));
        productionsCountry.setCountry(country);

        return productionsCountry;
    }
}