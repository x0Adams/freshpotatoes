package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GenreMovieId implements Serializable {
    private static final long serialVersionUID = -2949338528361746767L;
    @Column(name = "movie", nullable = false, length = 16)
    private String movie;

    @Column(name = "genre", nullable = false, length = 75)
    private String genre;

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenreMovieId entity = (GenreMovieId) o;
        return Objects.equals(this.movie, entity.movie) &&
                Objects.equals(this.genre, entity.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, genre);
    }
}