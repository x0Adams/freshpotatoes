package hu.freshPotatoes.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "country_continent", schema = "fresh_potatoes")
public class CountryContinent {
    @EmbeddedId
    private CountryContinentId id;

    @MapsId("country")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "country", nullable = false)
    private Country country;

    @MapsId("continent")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "continent", nullable = false)
    private Continent continent;

    public CountryContinentId getId() {
        return id;
    }

    public void setId(CountryContinentId id) {
        this.id = id;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Continent getContinent() {
        return continent;
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

}