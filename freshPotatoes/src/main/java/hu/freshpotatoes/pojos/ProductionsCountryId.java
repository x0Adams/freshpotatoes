package hu.freshpotatoes.pojos;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductionsCountryId implements Serializable {
    private static final long serialVersionUID = -2603571954175703323L;
    @Column(name = "movie", nullable = false)
    private Integer movie;

    @Column(name = "country", nullable = false)
    private Integer country;

    public Integer getMovie() {
        return movie;
    }

    public void setMovie(Integer movie) {
        this.movie = movie;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionsCountryId entity = (ProductionsCountryId) o;
        return Objects.equals(this.movie, entity.movie) &&
                Objects.equals(this.country, entity.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movie, country);
    }
}