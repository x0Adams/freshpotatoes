package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ActedId implements Serializable {
    private static final long serialVersionUID = -4035724308296276306L;
    @Column(name = "movie", nullable = false, length = 16)
    private String movie;

    @Column(name = "actor", nullable = false)
    private Integer actor;

    public String getMovie() {
        return movie;
    }

    public void setMovie(String movie) {
        this.movie = movie;
    }

    public Integer getActor() {
        return actor;
    }

    public void setActor(Integer actor) {
        this.actor = actor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActedId entity = (ActedId) o;
        return Objects.equals(this.movie, entity.movie) &&
                Objects.equals(this.actor, entity.actor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, actor);
    }
}