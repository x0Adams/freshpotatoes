package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DirectedId implements Serializable {
    private static final long serialVersionUID = 6826101212384698255L;
    @Column(name = "movie", nullable = false, length = 36)
    private String movie;

    @Column(name = "director", nullable = false)
    private Integer director;

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public Integer getDirector() {
        return director;
    }

    public void setDirector(Integer director) {
        this.director = director;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectedId entity = (DirectedId) o;
        return Objects.equals(this.movie, entity.movie) &&
                Objects.equals(this.director, entity.director);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, director);
    }
}