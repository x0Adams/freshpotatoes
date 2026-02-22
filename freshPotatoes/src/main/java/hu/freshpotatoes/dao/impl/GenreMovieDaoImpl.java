package hu.freshpotatoes.dao.impl;

import hu.freshpotatoes.dao.GenreMovieDao;
import hu.freshpotatoes.model.Genre;
import hu.freshpotatoes.model.GenreMovie;
import hu.freshpotatoes.model.GenreMovieId;
import hu.freshpotatoes.model.Movie;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenreMovieDaoImpl implements GenreMovieDao {

    private final DataSource dataSource;

    public GenreMovieDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<GenreMovie> findById(GenreMovieId id) {
        String sql = "SELECT movie, genre FROM fresh_potatoes.genre_movie WHERE movie = ? AND genre = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getMovie());
            stmt.setInt(2, id.getGenre());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToGenreMovie(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<GenreMovie> findAll() {
        String sql = "SELECT movie, genre FROM fresh_potatoes.genre_movie";
        List<GenreMovie> genreMovies = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                genreMovies.add(mapRowToGenreMovie(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return genreMovies;
    }

    @Override
    public GenreMovie save(GenreMovie entity) {
        String sql = "INSERT INTO fresh_potatoes.genre_movie (movie, genre) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, entity.getId().getMovie());
            stmt.setInt(2, entity.getId().getGenre());
            stmt.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(GenreMovie entity) {
    }

    @Override
    public void delete(GenreMovieId id) {
        String sql = "DELETE FROM fresh_potatoes.genre_movie WHERE movie = ? AND genre = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id.getMovie());
            stmt.setInt(2, id.getGenre());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private GenreMovie mapRowToGenreMovie(ResultSet rs) throws SQLException {
        GenreMovie genreMovie = new GenreMovie();
        GenreMovieId id = new GenreMovieId();
        id.setMovie(rs.getInt("movie"));
        id.setGenre(rs.getInt("genre"));
        genreMovie.setId(id);

        Movie movie = new Movie();
        movie.setId(rs.getInt("movie"));
        genreMovie.setMovie(movie);

        Genre genre = new Genre();
        genre.setId(rs.getInt("genre"));
        genreMovie.setGenre(genre);

        return genreMovie;
    }
}