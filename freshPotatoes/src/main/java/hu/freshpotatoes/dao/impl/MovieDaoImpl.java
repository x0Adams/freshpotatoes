package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.MovieDao;
import hu.freshpotatoes.model.Movie;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MovieDaoImpl implements MovieDao {

    private final DataSource dataSource;

    public MovieDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Movie> findById(Integer id) {
        String sql = "SELECT id, name, poster_path, duration, release_date, youtube_movie, google_knowledge_graph, trailer " +
                "FROM fresh_potatoes.movie WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToMovie(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Movie> findAll() {
        String sql = "SELECT id, name, poster_path, duration, release_date, youtube_movie, google_knowledge_graph, trailer " +
                "FROM fresh_potatoes.movie";
        List<Movie> movies = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                movies.add(mapRowToMovie(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return movies;
    }

    @Override
    public Movie save(Movie entity) {
        String sql = "INSERT INTO fresh_potatoes.movie (name, poster_path, duration, release_date, youtube_movie, google_knowledge_graph, trailer) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setMovieParameters(stmt, entity);
            stmt.executeUpdate();
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Movie entity) {
        String sql = "UPDATE fresh_potatoes.movie SET name = ?, poster_path = ?, duration = ?, release_date = ?, " +
                "youtube_movie = ?, google_knowledge_graph = ?, trailer = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setMovieParameters(stmt, entity);
            stmt.setInt(8, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM fresh_potatoes.movie WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMovieParameters(PreparedStatement stmt, Movie entity) throws SQLException {
        stmt.setString(1, entity.getName());
        stmt.setString(2, entity.getPosterPath());
        stmt.setInt(3, entity.getDuration());
        stmt.setDate(4, Date.valueOf(entity.getReleaseDate()));
        stmt.setString(5, entity.getYoutubeMovie());
        stmt.setString(6, entity.getGoogleKnowledgeGraph());
        stmt.setString(7, entity.getTrailer());
    }

    private Movie mapRowToMovie(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setName(rs.getString("name"));
        movie.setPosterPath(rs.getString("poster_path"));
        movie.setDuration(rs.getInt("duration"));
        movie.setReleaseDate(rs.getDate("release_date").toLocalDate());
        movie.setYoutubeMovie(rs.getString("youtube_movie"));
        movie.setGoogleKnowledgeGraph(rs.getString("google_knowledge_graph"));
        movie.setTrailer(rs.getString("trailer"));
        return movie;
    }
}