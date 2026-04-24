package hu.pogany.freshPotato.repository;

import hu.pogany.freshPotato.entity.Movie;
import hu.pogany.freshPotato.entity.ProductionsCountry;
import hu.pogany.freshPotato.entity.ProductionsCountryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionsCountryRepository extends JpaRepository<ProductionsCountry, ProductionsCountryId> {
    void deleteByMovie(Movie movie);
}

