package hu.freshpotatoes.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "productions_country", schema = "fresh_potatoes")
public class ProductionsCountry {
    @EmbeddedId
    private ProductionsCountryId id;

    @MapsId("movie")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "movie", nullable = false)
    private Movie movie;

    @MapsId("country")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "country", nullable = false)
    private Country country;

    public ProductionsCountryId getId() {
        return id;
    }

    public void setId(ProductionsCountryId id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

}