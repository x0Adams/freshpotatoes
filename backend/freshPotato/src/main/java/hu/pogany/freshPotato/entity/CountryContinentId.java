package hu.pogany.freshPotato.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CountryContinentId implements Serializable {
    private static final long serialVersionUID = -3390986668198843278L;
    @Column(name = "country_id", nullable = false)
    private Integer countryId;

    @Column(name = "continent_id", nullable = false)
    private Integer continentId;

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Integer getContinentId() {
        return continentId;
    }

    public void setContinentId(Integer continentId) {
        this.continentId = continentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountryContinentId entity = (CountryContinentId) o;
        return Objects.equals(this.countryId, entity.countryId) &&
                Objects.equals(this.continentId, entity.continentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryId, continentId);
    }
}