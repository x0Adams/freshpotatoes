package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductionsCountryId implements Serializable {
    private static final long serialVersionUID = -3316253654461001542L;
    @Column(name = "movie_id", nullable = false)
    private Integer movieId;

    @Column(name = "country_id", nullable = false)
    private Integer countryId;

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionsCountryId entity = (ProductionsCountryId) o;
        return Objects.equals(this.movieId, entity.movieId) &&
                Objects.equals(this.countryId, entity.countryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, countryId);
    }
}