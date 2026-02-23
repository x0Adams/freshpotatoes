package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.StaffToleInMovieDao;
import hu.freshpotatoes.model.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StaffToleInMovieDaoImpl implements StaffToleInMovieDao {

    private final DataSource dataSource;

    public StaffToleInMovieDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<StaffToleInMovie> findById(StaffToleInMovieId id) {
        String sql = "SELECT role, movie, staff FROM fresh_potatoes.staff_tole_in_movie WHERE role = ? AND movie = ? AND staff = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getRole());
            stmt.setInt(2, id.getMovie());
            stmt.setInt(3, id.getStaff());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToStaffToleInMovie(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<StaffToleInMovie> findAll() {
        String sql = "SELECT role, movie, staff FROM fresh_potatoes.staff_tole_in_movie";
        List<StaffToleInMovie> staffToleInMovies = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                staffToleInMovies.add(mapRowToStaffToleInMovie(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return staffToleInMovies;
    }

    @Override
    public StaffToleInMovie save(StaffToleInMovie entity) {
        String sql = "INSERT INTO fresh_potatoes.staff_tole_in_movie (role, movie, staff) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId().getRole());
            stmt.setInt(2, entity.getId().getMovie());
            stmt.setInt(3, entity.getId().getStaff());
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(StaffToleInMovie entity) {
    }

    @Override
    public void delete(StaffToleInMovieId id) {
        String sql = "DELETE FROM fresh_potatoes.staff_tole_in_movie WHERE role = ? AND movie = ? AND staff = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getRole());
            stmt.setInt(2, id.getMovie());
            stmt.setInt(3, id.getStaff());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private StaffToleInMovie mapRowToStaffToleInMovie(ResultSet rs) throws SQLException {
        StaffToleInMovie staffToleInMovie = new StaffToleInMovie();
        StaffToleInMovieId id = new StaffToleInMovieId();
        id.setRole(rs.getInt("role"));
        id.setMovie(rs.getInt("movie"));
        id.setStaff(rs.getInt("staff"));
        staffToleInMovie.setId(id);

        Role role = new Role();
        role.setId(rs.getInt("role"));
        staffToleInMovie.setRole(role);

        Movie movie = new Movie();
        movie.setId(rs.getInt("movie"));
        staffToleInMovie.setMovie(movie);

        Staff staff = new Staff();
        staff.setId(rs.getInt("staff"));
        staffToleInMovie.setStaff(staff);

        return staffToleInMovie;
    }
}