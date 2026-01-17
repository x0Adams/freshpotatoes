package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RateId implements Serializable {
    private static final long serialVersionUID = -765172746569342544L;
    @Column(name = "user", nullable = false, length = 36)
    private String user;

    @Column(name = "movie", nullable = false, length = 36)
    private String movie;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
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