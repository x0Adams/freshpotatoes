package hu.freshpotatoes.pojos;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GenreMovieId implements Serializable {
    private static final long serialVersionUID = 7278350271066140446L;
    @Column(name = "movie", nullable = false)
    private Integer movie;

    @Column(name = "genre", nullable = false)
    private Integer genre;

    public Integer getMovie() {
        return movie;
    }

    public void setMovie(Integer movie) {
        this.movie = movie;
    }

    public Integer getGenre() {
        return genre;
    }

    public void setGenre(Integer genre) {
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