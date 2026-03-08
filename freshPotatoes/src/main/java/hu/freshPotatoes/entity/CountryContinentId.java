package hu.freshPotatoes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CountryContinentId implements Serializable {
    private static final long serialVersionUID = -6887039496450956327L;
    @Column(name = "country", nullable = false)
    private Integer country;

    @Column(name = "continent", nullable = false)
    private Integer continent;

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public Integer getContinent() {
        return continent;
    }

    public void setContinent(Integer continent) {
        this.continent = continent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryContinentId entity = (CountryContinentId) o;
        return Objects.equals(this.country, entity.country) &&
                Objects.equals(this.continent, entity.continent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, continent);
    }
}