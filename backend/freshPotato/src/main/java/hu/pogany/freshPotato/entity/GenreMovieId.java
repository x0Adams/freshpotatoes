package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GenreMovieId implements Serializable {
    private static final long serialVersionUID = -892133774396273769L;
    @Column(name = "movie_id", nullable = false)
    private Integer movieId;

    @Column(name = "genre_id", nullable = false)
    private Integer genreId;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenreMovieId entity = (GenreMovieId) o;
        return Objects.equals(this.movieId, entity.movieId) &&
                Objects.equals(this.genreId, entity.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, genreId);
    }
}