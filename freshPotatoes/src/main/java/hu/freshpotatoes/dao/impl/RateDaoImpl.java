package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.RateDao;
import hu.freshpotatoes.model.Movie;
import hu.freshpotatoes.model.Rate;
import hu.freshpotatoes.model.RateId;
import hu.freshpotatoes.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RateDaoImpl implements RateDao {

    private final DataSource dataSource;

    public RateDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Rate> findById(RateId id) {
        String sql = "SELECT user, movie, rating FROM fresh_potatoes.rate WHERE user = ? AND movie = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getUser());
            stmt.setInt(2, id.getMovie());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRate(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Rate> findAll() {
        String sql = "SELECT user, movie, rating FROM fresh_potatoes.rate";
        List<Rate> rates = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rates.add(mapRowToRate(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return rates;
    }

    @Override
    public Rate save(Rate entity) {
        String sql = "INSERT INTO fresh_potatoes.rate (user, movie, rating) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId().getUser());
            stmt.setInt(2, entity.getId().getMovie());
            stmt.setByte(3, entity.getRating());
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Rate entity) {
        String sql = "UPDATE fresh_potatoes.rate SET rating = ? WHERE user = ? AND movie = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setByte(1, entity.getRating());
            stmt.setInt(2, entity.getId().getUser());
            stmt.setInt(3, entity.getId().getMovie());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(RateId id) {
        String sql = "DELETE FROM fresh_potatoes.rate WHERE user = ? AND movie = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getUser());
            stmt.setInt(2, id.getMovie());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Rate mapRowToRate(ResultSet rs) throws SQLException {
        Rate rate = new Rate();
        RateId id = new RateId();
        id.setUser(rs.getInt("user"));
        id.setMovie(rs.getInt("movie"));
        rate.setId(id);

        User user = new User();
        user.setId(rs.getInt("user"));
        rate.setUser(user);

        Movie movie = new Movie();
        movie.setId(rs.getInt("movie"));
        rate.setMovie(movie);

        rate.setRating(rs.getByte("rating"));

        return rate;
    }
}