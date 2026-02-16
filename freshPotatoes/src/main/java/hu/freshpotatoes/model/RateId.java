package hu.freshpotatoes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RateId implements Serializable {
    private static final long serialVersionUID = -934693514062868116L;
    @Column(name = "user", nullable = false)
    private Integer user;

    @Column(name = "movie", nullable = false)
    private Integer movie;

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getMovie() {
        return movie;
    }

    public void setMovie(Integer movie) {
        this.movie = movie;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateId entity = (RateId) o;
        return Objects.equals(this.user, entity.user) &&
                Objects.equals(this.movie, entity.movie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, movie);
    }
}